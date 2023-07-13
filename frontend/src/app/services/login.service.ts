import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders, HttpResponse} from "@angular/common/http";
import {Observable} from "rxjs";
import {map} from "rxjs/operators";
import {CookieService} from "ngx-cookie-service";

import {AuthenticatedUser} from "../models/authenticated-user";
import {AuthRequest} from "./models/auth-request";
import {EnvironmentService} from "../environment.service";

@Injectable({
  providedIn: 'root'
})
export class LoginService {

  private baseUrl: string = this.environmentService.getBackendUrl() + '/auth';
  private static readonly JWT_RENEW_MARGIN: number = 30000;
  private interval: NodeJS.Timeout | null;

  constructor(private http: HttpClient, private environmentService: EnvironmentService,
              private cookies: CookieService) {
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

  logout(): void {
    this.cookies.delete("jwt");
    this.cookies.delete("selectedLanguage");
    this.cookies.delete("jwt_expires");
    sessionStorage.clear();
  }

  public setJwtValue(token: string, expires: number): void {
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
    this.setIntervalRenew(jwt, expiration, callback);
  }

  private setIntervalRenew(jwt: string, timeout: number, callback: (jwt: string, expiration: number) => void): void {
    this.interval = setInterval((): void => {
      //Set current JWT.
      this.setJwtValue(jwt, timeout);
      this.renew().subscribe(
        (res: HttpResponse<AuthenticatedUser>): void => {
          const authToken: string | null = res.headers.get('authorization');
          let expiration: number = Number(res.headers.get('expires'));
          if (!authToken || !expiration) {
            throw new Error('Server returned invalid response');
          }
          if (isNaN(expiration)) {
            throw new Error('Server returned invalid expiration time');
          }
          expiration = expiration - (new Date()).getTime() - LoginService.JWT_RENEW_MARGIN;
          console.log(`Next token expiration time: ${expiration}`);
          callback(authToken, expiration);
          this.setIntervalRenew(authToken, expiration, callback);
        }
      )
    }, timeout)
  }

  private renew(): Observable<HttpResponse<AuthenticatedUser>> {
    return this.http.get<HttpResponse<AuthenticatedUser>>(`${this.baseUrl}/jwt/renew`);
  }
}
