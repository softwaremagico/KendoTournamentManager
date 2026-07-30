import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders, HttpResponse} from "@angular/common/http";
import {Observable} from "rxjs";
import {map, tap} from "rxjs/operators";

import {AuthenticatedUser} from "../models/authenticated-user";
import {AuthRequest} from "./models/auth-request";
import {EnvironmentService} from "../environment.service";
import {Router} from "@angular/router";
import {ActivityService} from "./rbac/activity.service";
import {AuthGuestRequest} from "./models/auth-guest-request";
import {TemporalToken} from "./models/temporal-token";
import {UserRoles} from "./rbac/user-roles";
import {UserSessionService} from "./user-session.service";
import {RbacService} from "./rbac/rbac.service";

/**
 * Angular service that manages authentication state for the Kendo Tournament Manager.
 *
 * Handles four authentication flows:
 * 1. **Standard login** — username + password → JWT + expiry stored in session storage.
 * 2. **Guest login** — tournament ID only → limited-privilege JWT (QR-code access).
 * 3. **Participant login** — participant token (from QR code) → self-service JWT.
 * 4. **Token renewal** — proactive background renewal scheduled
 *    {@link JWT_RENEW_MARGIN} ms before expiry.
 *
 * After a successful login the JWT is stored in session storage and the user is
 * routed to the home page. On logout the storage is cleared and the user is
 * redirected to the login page.
 *
 * Role-based access control is evaluated by {@link RbacService} and
 * {@link ActivityService} using the roles returned by the backend.
 */
@Injectable({
  providedIn: 'root'
})
export class LoginService {

  private readonly baseUrl: string = this.environmentService.getBackendUrl() + '/auth';
  static readonly JWT_RENEW_MARGIN: number = 20000;
  private interval: ReturnType<typeof setInterval> | null;

  constructor(private readonly http: HttpClient, private readonly environmentService: EnvironmentService,
              private readonly activityService: ActivityService, private readonly router: Router, private readonly userSessionService: UserSessionService) {
    if (this.getJwtExpirationValue() !== undefined && this.getJwtExpirationValue() > 0) {
      this.autoRenewToken(this.getJwtValue(), (this.getJwtExpirationValue() - Date.now()) - LoginService.JWT_RENEW_MARGIN,
        (): void => {
        });
    }
  }

  login(username: string, password: string): Observable<AuthenticatedUser> {
    const url: string = `${this.baseUrl}/public/login`;
    return this.http.post<AuthenticatedUser>(url, new AuthRequest(username, password), {
      headers: new HttpHeaders({'Content-Type': 'application/json'}),
      responseType: 'json',
      observe: 'response'
    })
      .pipe(
        map((response: HttpResponse<AuthenticatedUser>) => this.withAuthHeaders(response)));
  }

  loginAsGuest(tournamentId: number): Observable<AuthenticatedUser> {
    const url: string = `${this.baseUrl}/public/login/guest`;
    return this.http.post<AuthenticatedUser>(url, new AuthGuestRequest(tournamentId), {
      headers: new HttpHeaders({'Content-Type': 'application/json'}),
      responseType: 'json',
      observe: 'response'
    })
      .pipe(
        map((response: HttpResponse<AuthenticatedUser>) => this.withAuthHeaders(response)));
  }

  loginAsParticipant(temporalToken: string): Observable<AuthenticatedUser> {
    const url: string = `${this.baseUrl}/public/participant/token`;
    return this.http.post<AuthenticatedUser>(url, new TemporalToken(temporalToken), {
      headers: new HttpHeaders({'Content-Type': 'application/json'}),
      responseType: 'json',
      observe: 'response'
    })
      .pipe(
        map((response: HttpResponse<AuthenticatedUser>) => this.withAuthHeaders(response)));
  }

  private withAuthHeaders(response: HttpResponse<AuthenticatedUser>): AuthenticatedUser {
    const authenticatedUser: AuthenticatedUser = response.body as AuthenticatedUser;
    authenticatedUser.jwt = response.headers.get('Authorization') ?? '';
    authenticatedUser.expires = Number(response.headers.get('Expires') ?? 0);
    authenticatedUser.session = response.headers.get('X-Session') ?? '';
    return authenticatedUser;
  }

  setGuestUserSession(tournamentId: number, callback: (token: string, expiration: number) => void): void {
    this.loginAsGuest(tournamentId).subscribe({
      next: (authenticatedUser: AuthenticatedUser): void => {
        this.setAuthenticatedUser(authenticatedUser, callback);
        localStorage.setItem('account', 'guest');
        localStorage.setItem('tournamentId', tournamentId + "");
      },
      error: (): void => {
        this.router.navigate(["/"]);
      }
    });
  }

  setParticipantUserSession(temporalToken: string, callback: (token: string, expiration: number) => void): void {
    this.loginAsParticipant(temporalToken).subscribe({
      next: (authenticatedUser: AuthenticatedUser): void => {
        this.setAuthenticatedUser(authenticatedUser, callback);
        localStorage.setItem('account', 'participant');
      },
      error: (): void => {
        this.router.navigate(["/"]);
      }
    });
  }

  setAuthenticatedUser(authenticatedUser: AuthenticatedUser, callback: (token: string, expiration: number) => void): void {
    this.setJwtValue(authenticatedUser.jwt, authenticatedUser.expires);
    this.autoRenewToken(authenticatedUser.jwt, (authenticatedUser.expires - Date.now()) - LoginService.JWT_RENEW_MARGIN,
      (): void => {
      });
    this.activityService.setRoles(authenticatedUser.roles);
    this.userSessionService.setLocalUser(authenticatedUser);
    localStorage.setItem('username', authenticatedUser.username);
    localStorage.setItem('session', authenticatedUser.session);
    callback(authenticatedUser.jwt, authenticatedUser.expires);
  }

  public refreshDataFormJwt(): void {
    if (this.getJwtValue()) {
      this.getUserRoles().subscribe((_roles: string[]): void => {
        this.activityService.setRoles(UserRoles.getByKeys(_roles));
      });
    }
  }

  logout(): void {
    this.userSessionService.clearToken();
    this.activityService.setRoles([]);
    localStorage.removeItem('account');
    localStorage.removeItem('tournamentId');
  }

  private setJwtValue(token: string, expires: number): void {
    this.userSessionService.setAuthToken(token);
    this.userSessionService.setExpirationDate(expires)
  }

  public getJwtValue(): string | null {
    return this.userSessionService.getAuthToken();
  }

  public getJwtExpirationValue(): number {
    return Number(this.userSessionService.getExpirationDate());
  }

  public autoRenewToken(jwt: string | null, expiration: number, callback: (token: string, expiration: number) => void): void {
    this.clearRenewInterval();
    if (expiration > 0 && jwt != null) {
      this.setIntervalRenew(jwt, expiration, callback);
    }
  }

  private clearRenewInterval(): void {
    if (this.interval != null) {
      clearInterval(this.interval);
      this.interval = null;
    }
  }

  private stopAutoRenew(jwt: string, reason: string): void {
    console.error(reason);
    this.autoRenewToken(jwt, -1, (): void => {
    });
  }

  private getRenewedSession(response: HttpResponse<unknown>): { authToken: string, expiration: number } | null {
    const authToken: string | null = response.headers.get('Authorization');
    const expiration: number = Number(response.headers.get('Expires'));
    if (!authToken || !expiration || Number.isNaN(expiration)) {
      return null;
    }
    return {authToken, expiration};
  }

  private handleRenewResponse(jwt: string, response: HttpResponse<unknown>, callback: (jwt: string, expiration: number) => void): void {
    const renewedSession = this.getRenewedSession(response);
    if (!renewedSession) {
      this.stopAutoRenew(jwt, 'Server returned invalid renew response');
      return;
    }
    const renewValue: number = (renewedSession.expiration - Date.now()) - LoginService.JWT_RENEW_MARGIN;
    callback(renewedSession.authToken, renewedSession.expiration);
    this.setJwtValue(renewedSession.authToken, renewedSession.expiration);
    this.autoRenewToken(renewedSession.authToken, renewValue, callback);
  }

  private setIntervalRenew(jwt: string, timeout: number, callback: (jwt: string, expiration: number) => void): void {
    this.interval = setInterval((): void => {
      this.renew().subscribe({
        next: (response: HttpResponse<unknown>): void => {
          this.handleRenewResponse(jwt, response, callback);
        },
        error: (): void => {
          this.stopAutoRenew(jwt, 'JWT renewal failed');
        }
      });
    }, timeout);
  }

  getUserRoles(): Observable<string[]> {
    const url: string = `${this.baseUrl}/roles`;
    return this.http.get<string[]>(url)
      .pipe(
        tap({
          next: (_roles: string[]) => console.info(`Obtained '${_roles}' roles!`)
        })
      );
  }

  private renew(): Observable<HttpResponse<unknown>> {
    return this.http.get<unknown>(`${this.baseUrl}/jwt/renew`, {observe: 'response'}).pipe(
      tap({
        next: () => console.info(`Renewing JWT successfully!`)
      })
    );
  }
}
