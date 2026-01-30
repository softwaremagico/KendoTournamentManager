import {inject, Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot} from '@angular/router';
import {LoginService} from "../services/login.service";
import {BehaviorSubject} from "rxjs";
import {TournamentService} from "../services/tournament.service";

@Injectable({
  providedIn: 'root'
})
export class LoggedInService {

  //Pages that will not force a login to access.
  whiteListedPages: string[] = ["/tournaments/fights", "/participants/statistics"];

  public isUserLoggedIn: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

  constructor(private router: Router, public loginService: LoginService, private tournamentService: TournamentService) {
  }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    const context: string = state.url.substring(0, state.url.indexOf('?') > 0 ? state.url.indexOf('?') : state.url.length);
    const params: string = state.url.indexOf('?') > 0 ? state.url.substring(state.url.indexOf('?') + 1) : "";
    if (this.loginService.getJwtValue() || this.whiteListedPages.includes(context)) {
      //Read roles from JWT if it is a returning user.
      this.loginService.refreshDataFormJwt();
      // JWT Token exists, is a registered participant.
      this.isUserLoggedIn.next(true);
      return this.userLoginPageDependingOnRoles(context, params);
    }

    // Not logged in so redirect to login page with the return url
    this.router.navigate(['/login'], {queryParams: {returnUrl: state.url}});
    this.isUserLoggedIn.next(false);
    return false;
  }

  userLoginPageDependingOnRoles(context: string, params: string): boolean {
    if (this.loginService.getJwtValue()) {
      //Participant users must redirect to their statistics.
      if (localStorage.getItem('account') == 'participant'
        && (!context.startsWith('/participants/statistics') && !context.startsWith('/participants/fights'))) {
        this.router.navigate(['/participants/statistics']);
      } else if (localStorage.getItem('account') == 'guest' && !context.startsWith('/tournaments/fights')) {
        this.router.navigate(['/tournaments/fights']);
      }
      return true;
    }
    return this.whiteListedPages.includes(context);
  }
}

export const LoggedIn: CanActivateFn = (next: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean => {
  return inject(LoggedInService).canActivate(next, state);
}
