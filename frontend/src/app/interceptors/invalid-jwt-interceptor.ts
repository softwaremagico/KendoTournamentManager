import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from "@angular/common/http";
import {Observable} from "rxjs";
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

  intercept(request: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    return next.handle(request).pipe(
      catchError((error) => {
        //If on JWT, the IP is changed, launch a 409 error. 401 and 423 are for invalid or expired jwt. As Jwt is invalid now, logging again.
        if (error.status === 409 || error.status === 401 || error.status === 423) {
          this.loginService.logout();
          this.messageService.warningMessage("userLoggedOutMessage");
          this.router.navigate(['/login'], {queryParams: {returnUrl: "/tournaments"}});
        }
        throw error;
      })
    )
  }
}
