import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
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

@Injectable({
  providedIn: 'root'
})
export class LoginService {

  private baseUrl: string = this.environmentService.getBackendUrl() + '/auth';
  static readonly JWT_RENEW_MARGIN: number = 20000;
  private interval: NodeJS.Timeout | null;

  constructor(private http: HttpClient, private environmentService: EnvironmentService,
              private activityService: ActivityService, private router: Router, private userSessionService: UserSessionService) {
    if (this.getJwtExpirationValue() !== undefined && this.getJwtExpirationValue() > 0) {
      this.autoRenewToken(this.getJwtValue(), (this.getJwtExpirationValue() - (new Date()).getTime()) - LoginService.JWT_RENEW_MARGIN,
        (jwt: string, expires: number): void => {
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
        map((response: any) => {
          response.body.jwt = response.headers.get('Authorization');
          response.body.expires = response.headers.get('Expires');
          response.body.session = response.headers.get('X-Session');
          return response.body;
        }));
  }

  loginAsGuest(tournamentId: number): Observable<AuthenticatedUser> {
    const url: string = `${this.baseUrl}/public/login/guest`;
    return this.http.post<AuthenticatedUser>(url, new AuthGuestRequest(tournamentId), {
      headers: new HttpHeaders({'Content-Type': 'application/json'}),
      responseType: 'json',
      observe: 'response'
    })
      .pipe(
        map((response: any) => {
          response.body.jwt = response.headers.get('Authorization');
          response.body.expires = response.headers.get('Expires');
          response.body.session = response.headers.get('X-Session');
          return response.body;
        }));
  }

  loginAsParticipant(temporalToken: string): Observable<AuthenticatedUser> {
    const url: string = `${this.baseUrl}/public/participant/token`;
    return this.http.post<AuthenticatedUser>(url, new TemporalToken(temporalToken), {
      headers: new HttpHeaders({'Content-Type': 'application/json'}),
      responseType: 'json',
      observe: 'response'
    })
      .pipe(
        map((response: any) => {
          response.body.jwt = response.headers.get('Authorization');
          response.body.expires = response.headers.get('Expires');
          response.body.session = response.headers.get('X-Session');
          return response.body;
        }));
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
    this.autoRenewToken(authenticatedUser.jwt, (authenticatedUser.expires - (new Date()).getTime()) - LoginService.JWT_RENEW_MARGIN,
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
    if (this.interval != null) {
      clearInterval(this.interval);
      this.interval = null;
    }
    if (expiration > 0 && jwt != null) {
      this.setIntervalRenew(jwt, expiration, callback);
    }
  }

  private setIntervalRenew(jwt: string, timeout: number, callback: (jwt: string, expiration: number) => void): void {
    this.interval = setInterval((): void => {
      this.renew().subscribe(
        response => {
          if (!response) {
            console.error('No renew response!!!');
            this.autoRenewToken(jwt, -1, (jwt: string, expires: number): void => {
            });
            throw new Error('Server returned no response');
          }
          const authToken: string | null = response.headers.get('Authorization');
          let expiration: number = Number(response.headers.get('Expires'));
          if (!authToken || !expiration) {
            this.autoRenewToken(jwt, -1, (jwt: string, expires: number): void => {
            });
            throw new Error('Server returned invalid response');
          }
          if (isNaN(expiration)) {
            throw new Error('Server returned invalid expiration time');
          }
          const renewValue: number = (expiration - (new Date()).getTime()) - LoginService.JWT_RENEW_MARGIN;
          callback(authToken, expiration);
          //Set current JWT.
          this.setJwtValue(authToken, expiration);
          this.autoRenewToken(authToken, renewValue, callback);
        }
      )
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

  private renew(): Observable<any> {
    return this.http.get<any>(`${this.baseUrl}/jwt/renew`, {observe: 'response'}).pipe(
      tap({
        next: () => console.info(`Renewing JWT successfully!`)
      })
    );
  }
}
