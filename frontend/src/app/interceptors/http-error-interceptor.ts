import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from "@angular/common/http";
import {Observable, throwError} from "rxjs";
import {Router} from "@angular/router";
import {Injectable} from "@angular/core";
import {catchError} from "rxjs/operators";
import {LoginService} from "../services/login.service";
import {MessageService} from "../services/message.service";
import {EnvironmentService} from "../environment.service";
import {isAuthSessionErrorStatus, logoutAndRedirectToLogin} from "./auth-session-error";

@Injectable()
export class HttpErrorInterceptor implements HttpInterceptor {

  constructor(public router: Router, private readonly loginService: LoginService,
               private readonly messageService: MessageService, private readonly environmentService: EnvironmentService,
               ) {
  }

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(request).pipe(
      catchError((error: unknown) => {
        const httpError: { error?: unknown; ok: boolean; status?: number; url?: string } = error as { error?: unknown; ok: boolean; status?: number; url?: string };
        // Request-specific handlers render backend errors with context. Showing the
        // same error here caused duplicate snackbars for a single failed request.
        if (typeof httpError.status === 'number' && isAuthSessionErrorStatus(httpError.status)) {
          //Ensure errors only from Kendo Tournament (for future external calls).
          if (httpError.url?.startsWith(this.environmentService.getBackendUrl())) {
            logoutAndRedirectToLogin(this.router, this.loginService, this.messageService);
          }
        }
        return throwError(() => error);
      })
    )
  }
}
