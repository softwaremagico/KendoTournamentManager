import {inject, Injectable} from '@angular/core';
import {CanActivateFn, Router} from '@angular/router';
import {UserSessionService} from './user-session.service';
import {UserRoles} from './rbac/user-roles';
import {Constants} from '../constants';

@Injectable({providedIn: 'root'})
export class SuperAdminGuardService {
  constructor(private readonly router: Router, private readonly userSessionService: UserSessionService) {
  }

  canActivate(): boolean {
    if (this.userSessionService.getUser()?.roles?.includes(UserRoles.SUPER_ADMIN)) {
      return true;
    }
    this.router.navigate([Constants.PATHS.TOURNAMENTS.ROOT]);
    return false;
  }
}

export const SuperAdminGuard: CanActivateFn = (): boolean => inject(SuperAdminGuardService).canActivate();
