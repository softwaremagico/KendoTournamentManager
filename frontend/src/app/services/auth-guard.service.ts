import {inject, Injectable} from '@angular/core';
import {ActivatedRouteSnapshot, CanActivateFn, Router, RouterStateSnapshot} from "@angular/router";
import {Constants} from "../constants";
import {UserSessionService} from "./user-session.service";

@Injectable({
  providedIn: 'root'
})
export class AuthGuardService {

  constructor(private router: Router, private sessionService: UserSessionService) {
  }

  canActivate(): boolean {
    if (!this.sessionService.isTokenExpired()) {
      return true;
    }
    const queryParams: { [key: string]: string } = {};
    queryParams[Constants.PATHS.QUERY.EXPIRED] = "";
    this.router.navigate([`/login`], {queryParams: queryParams});
    return false;
  }
}

export const AuthGuard: CanActivateFn = (next: ActivatedRouteSnapshot, state: RouterStateSnapshot): boolean => {
  return inject(AuthGuardService).canActivate();
}
