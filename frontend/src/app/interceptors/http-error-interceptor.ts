import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from "@angular/common/http";
import {Observable} from "rxjs";
import {Router} from "@angular/router";
import {Injectable} from "@angular/core";
import {catchError} from "rxjs/operators";
import {LoginService} from "../services/login.service";
import {MessageService} from "../services/message.service";
import {EnvironmentService} from "../environment.service";
import {ErrorHandler} from "@biit-solutions/wizardry-theme/utils";
import {TranslocoService} from "@ngneat/transloco";
import {BiitSnackbarService} from "@biit-solutions/wizardry-theme/info";

@Injectable()
export class HttpErrorInterceptor implements HttpInterceptor {

  constructor(public router: Router, private loginService: LoginService,
              private messageService: MessageService, private environmentService: EnvironmentService,
              protected transloco: TranslocoService,
              private biitSnackbarService: BiitSnackbarService,) {
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(
      catchError((error) => {
        if (error.error instanceof Error) {
          // A client-side or network error occurred. Handle it accordingly.
          console.error('An error occurred:', error.error.message);
        } else {
          // Log error.
          if (error.error && !error.ok) {
            ErrorHandler.notify(error, this.transloco, this.biitSnackbarService);
            console.error(`Backend returned code ${error.status}, body was: ${error.error}`);
            //Set to 'ok' so it is not handled again by local error handler.
            error.ok = true;
          }
        }
        if (error.status === 409 || error.status === 401 || error.status === 423) {
          //Ensure errors only from Kendo Tournament (for future external calls).
          if (error.url.startsWith(this.environmentService.getBackendUrl())) {
            this.loginService.logout();
            this.messageService.warningMessage("userLoggedOutMessage");
            this.router.navigate(['/login'], {queryParams: {returnUrl: "/tournaments"}})
              .then((r: boolean) => console.log("User redirected to login window."));
          } else {
            console.error(`Error from ${error.url}`);
          }
        }
        throw error;
      })
    )
  }
}
