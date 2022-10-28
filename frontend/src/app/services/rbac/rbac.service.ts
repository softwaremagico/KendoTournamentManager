import {Injectable} from '@angular/core';
import {UserService} from "../user.service";
import {RbacActivity} from "./rbac.activity";

@Injectable({
  providedIn: 'root'
})
export class RbacService {

  private roles: string[];
  private activities: RbacActivity[];

  constructor(private userService: UserService) {
  }

  public setRoles(roles: string[]): void {
    this.roles = roles.map(role => role.toLowerCase());
    this.activities = this.getActivities(roles);
  }

  public isAllowedTo(rbacActivity: RbacActivity): boolean {
    return this.activities.indexOf(rbacActivity) >= 0;
  }

  private getActivities(roles: string[]): RbacActivity[] {
    let activities: RbacActivity[] = this.getGuestActivities();
    for (const role of roles) {
      switch (role) {
        case 'admin':
          activities = activities.concat(this.getAdminActivities());
          break;
        case 'editor':
          activities = activities.concat(this.getEditorActivities());
          break;
        case 'viewer':
          activities = activities.concat(this.getViewerActivities());
          break;
      }
    }
    return activities;
  }

  private getAdminActivities(): RbacActivity[] {
    return RbacActivity.toArray();
  }

  private getEditorActivities(): RbacActivity[] {
    return [];
  }

  private getViewerActivities(): RbacActivity[] {
    return [];
  }

  private getGuestActivities(): RbacActivity[] {
    return [];
  }

}
