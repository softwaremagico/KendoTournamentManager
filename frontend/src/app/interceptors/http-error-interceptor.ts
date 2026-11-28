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
        const url: string = httpError.url ?? '';
        // Requests that work without credentials (public registration, logging,
        // authentication) must show their own error instead of jumping to login.
        const isPublicRequest: boolean = url.includes('/registration/public/')
          || url.includes('/logger/')
          || url.includes('/auth/public/');
        // Only a session that actually exists can become invalid. Anonymous
        // failures are handled by the component that made the request.
        const hasSession: boolean = !!this.loginService.getJwtValue();
        if (typeof httpError.status === 'number' && isAuthSessionErrorStatus(httpError.status)
          && !isPublicRequest && hasSession
          //Ensure errors only from Kendo Tournament (for future external calls).
          && url.startsWith(this.environmentService.getBackendUrl())) {
          logoutAndRedirectToLogin(this.router, this.loginService, this.messageService);
        }
        return throwError(() => error);
      })
    )
  }
}
