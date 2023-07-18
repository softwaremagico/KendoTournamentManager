import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivate, Router, RouterStateSnapshot} from '@angular/router';
import {LoginService} from "../services/login.service";
import {BehaviorSubject} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class LoggedInService implements CanActivate {

  public isUserLoggedIn: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

  constructor(private router: Router, public loginService: LoginService) {
  }

  canActivate(_route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    if (this.loginService.getJwtValue()) {
      // JWT Token exists, is a registered participant.
      this.isUserLoggedIn.next(true);
      return true;
    }

    console.error("Invalid jwt")

    // not logged in so redirect to login page with the return url
    this.router.navigate(['/login'], {queryParams: {returnUrl: state.url}});
    this.isUserLoggedIn.next(false);
    return false;
  }
}
