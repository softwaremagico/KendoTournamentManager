import {inject, Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot} from '@angular/router';
import {LoginService} from "../services/login.service";
import {BehaviorSubject} from "rxjs";
import {TournamentService} from "../services/tournament.service";
import {Tournament} from "../models/tournament";

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
    if (this.loginService.getJwtValue() || this.whiteListedPages.includes(context)) {
      //Read roles from JWT if it is a returning user.
      this.loginService.refreshDataFormJwt();
      // JWT Token exists, is a registered participant.
      this.isUserLoggedIn.next(true);
      //return this.userLoginPageDependingOnRoles(context);
      return true;
    }

    // Not logged in so redirect to login page with the return url
    this.router.navigate(['/login'], {queryParams: {returnUrl: state.url}});
    this.isUserLoggedIn.next(false);
    return false;
  }

  userLoginPageDependingOnRoles(context: string): boolean {
    if (this.loginService.getJwtValue()) {
      this.loginService.getUserRoles().subscribe((_roles: String[]): void => {
        if (_roles.includes("viewer") || _roles.includes("editor") || _roles.includes("admin")) {
          // Do nothing and navigate as usual.
        } else if (_roles.includes("guest")) {
          //Gets last tournament and redirects to fight list.
          this.tournamentService.getLastUnlockedTournament().subscribe((_tournament: Tournament): void => {
            //Path '/tournaments/fights' and '/fights/championship' does not call  LoggedInService to avoid redirect loops.
            if (_tournament) {
              this.router.navigate(['/tournaments/fights'], {state: {tournamentId: _tournament.id}});
            } else {
              this.router.navigate(['/login']);
            }
          });
        } else if (_roles.includes("participant")) {
          this.router.navigate(['/participants/statistics']);
        }
      });
      return true;
    }
    return false;
  }
}

export const LoggedIn: CanActivateFn = (next: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean => {
  return inject(LoggedInService).canActivate(next, state);
}
