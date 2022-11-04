import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
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

  private baseUrl = this.environmentService.getBackendUrl() + '/auth/public';

  httpOptions: { headers: HttpHeaders };

  constructor(private http: HttpClient, private environmentService: EnvironmentService,
              private cookies: CookieService) {
    this.refreshHeader();
  }

  login(username: string, password: string): Observable<AuthenticatedUser> {
    const url: string = `${this.baseUrl}/login`;
    return this.http.post<AuthenticatedUser>(url, new AuthRequest(username, password), {
      headers: new HttpHeaders({'Content-Type': 'application/json'}),
      responseType: 'json',
      observe: 'response'
    })
      .pipe(
        map((response: any) => {
          response.body.jwt = response.headers.get('Authorization');
          return response.body;
        }));
  }

  logout(): void {
    this.cookies.delete("jwt");
    this.cookies.delete("selectedLanguage");
    sessionStorage.clear();
  }

  private refreshHeader() {
    this.httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + this.getJwtValue()
      })
    };
  }

  setJwtValue(token: string) {
    this.cookies.set("jwt", token);
    this.refreshHeader();
  }

  getJwtValue(): string {
    return this.cookies.get("jwt");
  }
}
