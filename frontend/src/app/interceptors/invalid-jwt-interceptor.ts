import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from "@angular/common/http";
import {Observable, throwError} from "rxjs";
import {Router} from "@angular/router";
import {Injectable} from "@angular/core";
import {catchError} from "rxjs/operators";
import {LoginService} from "../services/login.service";
import {MessageService} from "../services/message.service";
import {isAuthSessionErrorStatus, logoutAndRedirectToLogin} from "./auth-session-error";

@Injectable()
export class InvalidJwtInterceptor implements HttpInterceptor {

  constructor(public router: Router, private readonly loginService: LoginService,
              private readonly messageService: MessageService) {
  }

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(request).pipe(
      catchError((error: unknown) => {
        //If on JWT, the IP is changed, launch a 409 error. 401 and 423 are for invalid or expired jwt. As Jwt is invalid now, logging again.
        const httpError: { status?: number } = typeof error === 'object' && error !== null ? error : {};
        if (typeof httpError.status === 'number' && isAuthSessionErrorStatus(httpError.status)) {
          logoutAndRedirectToLogin(this.router, this.loginService, this.messageService);
        }
        return throwError(() => error);
      })
    )
  }
}
