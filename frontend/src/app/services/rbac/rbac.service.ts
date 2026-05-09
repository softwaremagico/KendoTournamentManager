import {Injectable} from '@angular/core';
import {RbacActivity} from "./rbac.activity";
import {UserService} from "../user.service";
import {UserRoles} from "./user-roles";
import {CookieService} from "ngx-cookie-service";
import {ActivityService} from "./activity.service";

/**
 * Angular service providing role-based access control (RBAC) checks for the UI.
 *
 * Acts as a thin facade over {@link ActivityService}. On initialisation it fetches
 * the current user's roles from the backend and passes them to
 * {@link ActivityService#setRoles} so that all subsequent {@link isAllowed} calls
 * reflect the server-assigned permissions.
 *
 * Roles are cached in a cookie so that they survive page refreshes without requiring
 * a network call on every navigation.
 *
 * Usage:
 * ```ts
 * if (this.rbacService.isAllowed(RbacActivity.EDIT_TOURNAMENT)) { ... }
 * ```
 */
@Injectable({
  providedIn: 'root'
})
export class RbacService {

  constructor(private userService: UserService, private cookies: CookieService, private activityService: ActivityService) {
    this.getRoles();
  }

  public getRoles(): void {
    if (this.cookies.get("jwt")) {
      this.userService.getRoles().subscribe((_roles: UserRoles[]): void => {
        this.setRoles(_roles);
      });
    }
  }

  public setRoles(roles: UserRoles[]): void {
    this.activityService.setRoles(roles);
  }

  public isAllowed(activity: RbacActivity | undefined): boolean {
    return this.activityService.isAllowed(activity);
  }

  public getActivities() {
    return this.activityService.activities;
  }

}
