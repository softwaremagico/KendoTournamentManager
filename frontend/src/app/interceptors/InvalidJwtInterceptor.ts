import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from "@angular/common/http";
import {Observable, throwError} from "rxjs";
import {Router} from "@angular/router";
import {Injectable} from "@angular/core";
import {catchError} from "rxjs/operators";
import {LoginService} from "../services/login.service";
import {MessageService} from "../services/message.service";

@Injectable()
export class InvalidJwtInterceptor implements HttpInterceptor {

  constructor(public router: Router, private loginService: LoginService,
              private messageService: MessageService) {
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {

    return next.handle(req).pipe(
      catchError((error) => {
        //If on JWT, the IP is changed, launch a 409 error. As Jwt is invalid now, logging again.
        if (error.status === 409) {
          this.loginService.logout();
          this.messageService.warningMessage("userloggedOutMessage");
          this.router.navigate(['/login'], {queryParams: {returnUrl: "/tournaments"}});
        }
        return throwError(error.message);
      })
    )
  }
}
