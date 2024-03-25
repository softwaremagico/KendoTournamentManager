import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Observable} from "rxjs";
import {map, tap} from "rxjs/operators";
import {CookieService} from "ngx-cookie-service";

import {AuthenticatedUser} from "../models/authenticated-user";
import {AuthRequest} from "./models/auth-request";
import {EnvironmentService} from "../environment.service";
import {Router} from "@angular/router";
import {ActivityService} from "./rbac/activity.service";
import {AuthGuestRequest} from "./models/auth-guest-request";

@Injectable({
  providedIn: 'root'
})
export class LoginService {

  private baseUrl: string = this.environmentService.getBackendUrl() + '/auth';
  static readonly JWT_RENEW_MARGIN: number = 20000;
  private interval: NodeJS.Timeout | null;

  constructor(private http: HttpClient, private environmentService: EnvironmentService,
              private cookies: CookieService, private activityService: ActivityService, private router: Router) {
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
          return response.body;
        }));
  }

  //Basic login for guests.
  setUserSession(username: string, password: string): void {
    this.login(username, password).subscribe({
      next: (authenticatedUser: AuthenticatedUser): void => {
        this.setJwtValue(authenticatedUser.jwt, authenticatedUser.expires);
        this.autoRenewToken(authenticatedUser.jwt, (authenticatedUser.expires - (new Date()).getTime()) - LoginService.JWT_RENEW_MARGIN,
          (): void => {
          });
        this.activityService.setRoles(authenticatedUser.roles);
        localStorage.setItem('username', username);
      },
      error: (error): void => {
        this.router.navigate(["/"]);
      }
    });
  }

  setGuestUserSession(tournamentId: number, callback: (token: string, expiration: number) => void): void {
    this.loginAsGuest(tournamentId).subscribe({
      next: (authenticatedUser: AuthenticatedUser): void => {
        this.setJwtValue(authenticatedUser.jwt, authenticatedUser.expires);
        this.autoRenewToken(authenticatedUser.jwt, (authenticatedUser.expires - (new Date()).getTime()) - LoginService.JWT_RENEW_MARGIN,
          (): void => {
          });
        this.activityService.setRoles(authenticatedUser.roles);
        localStorage.setItem('username', 'guest');
        callback(authenticatedUser.jwt, authenticatedUser.expires);
      },
      error: (error): void => {
        this.router.navigate(["/"]);
      }
    });
  }

  logout(): void {
    this.cookies.delete("jwt");
    this.cookies.delete("selectedLanguage");
    this.cookies.delete("jwt_expires");
    sessionStorage.clear();
  }

  public setJwtValue(token: string, expires: number): void {
    localStorage.setItem("jwt", token);
    this.cookies.set("jwt", token);
    this.cookies.set("jwt_expires", expires.toString());
  }

  public getJwtValue(): string {
    return this.cookies.get("jwt");
  }

  getJwtExpirationValue(): number {
    return Number(this.cookies.get("jwt_expires"));
  }

  public autoRenewToken(jwt: string, expiration: number, callback: (token: string, expiration: number) => void): void {
    if (this.interval != null) {
      clearInterval(this.interval);
      this.interval = null;
    }
    if (expiration > 0) {
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
          const authToken: string | null = response.headers.get('authorization');
          let expiration: number = Number(response.headers.get('expires'));
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

  getUserRoles(): Observable<String[]> {
    const url: string = `${this.baseUrl}/roles`;
    return this.http.get<String[]>(url)
      .pipe(
        tap({
          next: (_roles: String[]) => console.info(`Obtained '${_roles}' roles!`)
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
