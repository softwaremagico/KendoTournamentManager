import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {Observable, of} from "rxjs";
import {map} from "rxjs/operators";
import {CookieService} from "ngx-cookie-service";

import {AuthenticatedUser} from "../models/authenticated-user";
import {AuthRequest} from "./models/auth-request";
import {LoggerService} from "../logger.service";
import {EnvironmentService} from "../environment.service";

@Injectable({
  providedIn: 'root'
})
export class AuthenticatedUserService {

  private baseUrl = this.environmentService.getBackendUrl() + '/api/public';

  constructor(private http: HttpClient, private environmentService: EnvironmentService, private loggerService: LoggerService,
              private cookies: CookieService) {
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
          const jwt = response.headers.get('Authorization');
          response.body.jwt = jwt;
          return response.body;
        }));
  }

  setToken(token: string) {
    this.cookies.set("token", token);
  }

  getToken(): string {
    return this.cookies.get("token");
  }

  private log(message: string) {
    this.loggerService.add(`UserService: ${message}`);
  }

  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {

      // TODO: send the error to remote logging infrastructure
      console.error(error); // log to console instead

      // TODO: better job of transforming error for user consumption
      this.log(`${operation} failed: ${error.message}`);

      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }
}
