import { HttpEvent, HttpHandler, HttpInterceptor, HttpRequest } from "@angular/common/http";
import {Observable} from "rxjs";
import {Injectable} from "@angular/core";
import {LoginService} from "../services/login.service";

@Injectable()
export class HeaderInterceptor implements HttpInterceptor {

  constructor(private readonly loginService: LoginService) {
  }

  intercept(req: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    const session = localStorage.getItem('session') ? localStorage.getItem('session') + "" : "";
    const token = this.loginService.getJwtValue();
    const request: HttpRequest<unknown> = req.clone({
      headers: token ? req.headers.set('Authorization', 'Bearer ' + token).set('X-Session', session)
        : req.headers.set('X-Session', session),
    });
    return next.handle(request);
  }
}
