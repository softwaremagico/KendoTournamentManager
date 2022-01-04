import {Injectable} from '@angular/core';
import {Router, CanActivate, ActivatedRouteSnapshot, RouterStateSnapshot} from '@angular/router';
import {AuthenticatedUserService} from "../services/authenticated-user.service";

@Injectable({
  providedIn: 'root'
})
export class LoggedInService implements CanActivate {

  constructor(private router: Router, public authenticatedUserService: AuthenticatedUserService) {
  }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot) {
    if (this.authenticatedUserService.getToken()) {
      // JWT Token exists, is a registered user.
      return true;
    }

    // not logged in so redirect to login page with the return url
    this.router.navigate(['/login'], {queryParams: {returnUrl: state.url}});
    return false;
  }
}
