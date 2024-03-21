import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from '@angular/router';
import {LoginService} from "../services/login.service";
import {BehaviorSubject} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class LoggedInService implements CanActivate {

  //Pages that will not force a login to access.
  whiteListedPages: string[] = ["/tournaments/fights"];

  public isUserLoggedIn: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

  constructor(private router: Router, public loginService: LoginService) {
  }

  canActivate(_route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    const context: string = state.url.substring(0, state.url.indexOf('?') > 0 ? state.url.indexOf('?') : state.url.length);
    if (this.loginService.getJwtValue() || this.whiteListedPages.includes(context)) {
      // JWT Token exists, is a registered participant.
      this.isUserLoggedIn.next(true);
      return true;
    }

    console.info("JWT not defined")

    // not logged in so redirect to login page with the return url
    this.router.navigate(['/login'], {queryParams: {returnUrl: state.url}});
    this.isUserLoggedIn.next(false);
    return false;
  }
}
