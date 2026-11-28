import {Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, Router, RouterStateSnapshot} from '@angular/router';
import {LoginService} from "../services/login.service";
import {BehaviorSubject} from "rxjs";
import {TournamentService} from "../services/tournament.service";

@Injectable({
  providedIn: 'root'
})
export class LoggedInService {

  //Pages that will not force a login to access.
  whiteListedPages: string[] = ["/tournaments/fights", "/participants/statistics", "/register"];

  public isUserLoggedIn: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

  constructor(private readonly router: Router, public loginService: LoginService, private readonly tournamentService: TournamentService) {
  }

  canActivate(route: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean {
    const context: string = this.contextOf(state.url);
    const params: string = state.url.indexOf('?') > 0 ? state.url.substring(state.url.indexOf('?') + 1) : "";
    if (this.loginService.getJwtValue() || this.isPublicPage(context)) {
      //Read roles from JWT if it is a returning user.
      this.loginService.refreshDataFormJwt();
      // JWT Token exists, is a registered participant.
      this.isUserLoggedIn.next(true);
      return this.userLoginPageDependingOnRoles(context, params);
    }

    // Not logged in so redirect to login page with the return url
    void this.router.navigate(['/login'], {queryParams: {returnUrl: state.url}});
    this.isUserLoggedIn.next(false);
    return false;
  }

  userLoginPageDependingOnRoles(context: string, params: string): boolean {
    const route: string = this.contextOf(context);
    if (this.isPublicRegistrationPage(route)) {
      //A registration link is public and must not be redirected by role-based rules.
      return true;
    }
    if (this.loginService.getJwtValue()) {
      //Participant users must redirect to their statistics.
      if (localStorage.getItem('account') === 'participant'
        && (!route.startsWith('/participants/statistics') && !route.startsWith('/participants/fights'))) {
        void this.router.navigate(['/participants/statistics']);
      } else if (localStorage.getItem('account') === 'guest' && !route.startsWith('/tournaments/fights')) {
        void this.router.navigate(['/tournaments/fights']);
      }
      return true;
    }
    return this.isPublicPage(route);
  }

  private isPublicPage(context: string): boolean {
    return this.whiteListedPages.includes(context) || this.isPublicRegistrationPage(context);
  }

  private isPublicRegistrationPage(context: string): boolean {
    return context === '/register' || context.startsWith('/register/');
  }

  private contextOf(url: string): string {
    //With hash routing a literal '#/register?...' can reach the guard.
    const withoutFragment: string = url.startsWith('/#') ? url.substring(1) : url;
    const context: string = withoutFragment.split('?', 1)[0].replace(/\/+$/, '');
    return context || '/';
  }
}
