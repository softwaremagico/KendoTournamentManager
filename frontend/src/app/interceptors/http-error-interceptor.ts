import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from "@angular/common/http";
import {Observable, throwError} from "rxjs";
import {Router} from "@angular/router";
import {Injectable} from "@angular/core";
import {catchError} from "rxjs/operators";
import {LoginService} from "../services/login.service";
import {MessageService} from "../services/message.service";

@Injectable()
export class HttpErrorInterceptor implements HttpInterceptor {

  constructor(public router: Router, private loginService: LoginService,
              private messageService: MessageService) {
  }

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(
      catchError((error) => {
        if (error.error instanceof Error) {
          // A client-side or network error occurred. Handle it accordingly.
          console.error('An error occurred:', error.error.message);
        } else {
          // Log error.
          console.error(`Backend returned code ${error.status}, body was: ${error.error}`);
        }
        if (error.status === 409 || error.status === 401 || error.status === 423) {
          this.loginService.logout();
          this.messageService.warningMessage("userLoggedOutMessage");
          this.router.navigate(['/login'], {queryParams: {returnUrl: "/tournaments"}});
        }
        return throwError(error.message);
      })
    )
  }
}
