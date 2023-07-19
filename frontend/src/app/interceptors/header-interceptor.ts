import {HttpEvent, HttpHandler, HttpInterceptor, HttpRequest} from "@angular/common/http";
import {Observable} from "rxjs";
import {Injectable} from "@angular/core";
import {LoginService} from "../services/login.service";

@Injectable()
export class HeaderInterceptor implements HttpInterceptor {

  constructor(private loginService: LoginService) {
  }

  intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    const request: HttpRequest<any> = req.clone({
      headers: req.headers.append('Authorization', 'Bearer ' + this.loginService.getJwtValue()),
    });
    return next.handle(request);
  }
}
