import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from "@angular/common/http";
import {Observable, throwError} from "rxjs";
import {Router} from "@angular/router";
import {Injectable} from "@angular/core";
import {catchError} from "rxjs/operators";
import {LoginService} from "../services/login.service";
import {MessageService} from "../services/message.service";
import {EnvironmentService} from "../environment.service";
import {ErrorHandler} from "@biit-solutions/wizardry-theme/utils";
import {TranslocoService} from "@jsverse/transloco";
import {BiitSnackbarService} from "@biit-solutions/wizardry-theme/info";
import {isAuthSessionErrorStatus, logoutAndRedirectToLogin} from "./auth-session-error";

@Injectable()
export class HttpErrorInterceptor implements HttpInterceptor {

  constructor(public router: Router, private readonly loginService: LoginService,
              private readonly messageService: MessageService, private readonly environmentService: EnvironmentService,
              protected readonly transloco: TranslocoService,
              private readonly biitSnackbarService: BiitSnackbarService,) {
  }

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    return next.handle(request).pipe(
      catchError((error: unknown) => {
        const httpError: { error?: unknown; ok: boolean; status?: number; url?: string } = error as { error?: unknown; ok: boolean; status?: number; url?: string };
        if (httpError.error instanceof Error) {
          // A client-side or network error occurred. Handle it accordingly.
        } else {
          // Log error.
          if (httpError.error && !httpError.ok) {
            ErrorHandler.notify(error as never, this.transloco as never, this.biitSnackbarService);
          }
        }
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
