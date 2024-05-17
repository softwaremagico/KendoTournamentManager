import {Injectable} from '@angular/core';
import {RbacActivity} from "./rbac.activity";
import {UserService} from "../user.service";
import {UserRoles} from "./user-roles";
import {CookieService} from "ngx-cookie-service";
import {ActivityService} from "./activity.service";

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
