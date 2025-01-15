import {Injectable} from '@angular/core';
import {Observable} from "rxjs";
import {AuthenticatedUser} from "../models/authenticated-user";
import {catchError, map, tap} from "rxjs/operators";
import {HttpClient, HttpErrorResponse} from "@angular/common/http";
import {EnvironmentService} from "../environment.service";
import {CookieService} from "ngx-cookie-service";
import {SystemOverloadService} from "./notifications/system-overload.service";
import {LoggerService} from "./logger.service";
import {MessageService} from "./message.service";
import {LoginService} from "./login.service";
import {UserRoles} from "./rbac/user-roles";

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private baseUrl: string = this.environmentService.getBackendUrl() + '/auth';

  constructor(private http: HttpClient, private environmentService: EnvironmentService,
              private cookies: CookieService, private systemOverloadService: SystemOverloadService,
              public loginService: LoginService,
              private loggerService: LoggerService, private messageService: MessageService) {

  }

  getAll(): Observable<AuthenticatedUser[]> {
    const url: string = `${this.baseUrl}/register`;
    return this.http.get<AuthenticatedUser[]>(url)
      .pipe(
        map((_users: any) => {
          for (let user of _users) {
            user.roles = UserRoles.getByKeys(user.roles);
          }
          return _users;
        }),
        tap({
          next: () => this.loggerService.info(`fetched all users`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<AuthenticatedUser[]>(`gets all`))
      );
  }

  add(authenticatedUser: AuthenticatedUser): Observable<AuthenticatedUser> {
    const url: string = `${this.baseUrl}/register`;
    return this.http.post<AuthenticatedUser>(url, authenticatedUser)
      .pipe(
        tap({
          next: (_authenticatedUser: AuthenticatedUser) => {
            this.loggerService.info(`adding user ${_authenticatedUser}`);
            this.messageService.infoMessage("infoAuthenticatedUserStored");
          },
          error: (error: { status: any; }): void => {
            this.systemOverloadService.isBusy.next(false);
            if (error instanceof HttpErrorResponse) {
              switch (error.status) {
                case 400:
                  this.messageService.errorMessage("errorUserAlreadyExists");
                  break;
                default:
                  throw error;
              }
            }
          },
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<AuthenticatedUser>(`adding ${authenticatedUser}`))
      );
  }

  update(authenticatedUser: AuthenticatedUser): Observable<AuthenticatedUser> {
    const url: string = `${this.baseUrl}/register`;
    return this.http.patch<AuthenticatedUser>(url, authenticatedUser)
      .pipe(
        tap({
          next: (_authenticatedUser: AuthenticatedUser) => this.loggerService.info(`updating user ${_authenticatedUser}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<AuthenticatedUser>(`updating ${authenticatedUser}`))
      );
  }

  delete(authenticatedUser: AuthenticatedUser): Observable<AuthenticatedUser> {
    const url: string = `${this.baseUrl}/register/` + authenticatedUser.username;
    return this.http.delete<AuthenticatedUser>(url)
      .pipe(
        tap({
          next: () => this.loggerService.info(`deleting user ${authenticatedUser}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<AuthenticatedUser>(`delete ${authenticatedUser}`))
      );
  }

  updatePassword(oldPassword: string, newPassword: string): Observable<void> {
    const url: string = `${this.baseUrl}/password`;
    return this.http.post<void>(url, {
      oldPassword: oldPassword,
      newPassword: newPassword
    })
      .pipe(
        tap({
          next: () => this.loggerService.info(`Updating password!`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<void>(`Updating password!`))
      );
  }

  getRoles(): Observable<UserRoles[]> {
    const url: string = `${this.baseUrl}/roles`;
    return this.http.get<UserRoles[]>(url)
      .pipe(
        tap({
          next: () => this.loggerService.info(`Getting roles for user`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        //catchError(this.messageService.handleError<UserRoles[]>(`Roles cannot be retrieved!`))
      );
  }
}
