import {Injectable} from '@angular/core';
import {RbacActivity} from "./rbac.activity";
import {UserService} from "../user.service";
import {UserRoles} from "./user-roles";
import {LoginService} from "../login.service";

@Injectable({
  providedIn: 'root'
})
export class RbacService {

  activities: RbacActivity[] = [];

  constructor(private userService: UserService, private loginService: LoginService) {
    this.getRoles();
  }

  public getRoles(): void {
    if (this.loginService.getJwtValue()) {
      this.userService.getRoles().subscribe((_roles: UserRoles[]): void => {
        this.setRoles(_roles);
      });
    }
  }

  public setRoles(roles: UserRoles[]): void {
    this.activities = this.getActivities(roles);
  }

  public isAllowed(activity: RbacActivity | undefined): boolean {
    if (!activity || !this.activities) {
      return false;
    }
    return this.activities.includes(activity);
  }

  private getActivities(roles: string[]): RbacActivity[] {
    let activities: RbacActivity[] = this.getGuestActivities();
    for (const role of roles) {
      switch (role.toLowerCase()) {
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

  private removeActivity(activities: RbacActivity[], activityToRemove: RbacActivity): void {
    const index: number = activities.indexOf(activityToRemove, 0);
    if (index > -1) {
      activities.splice(index, 1);
    }
  }

  private getAdminActivities(): RbacActivity[] {
    return RbacActivity.toArray();
  }

  private getEditorActivities(): RbacActivity[] {
    const adminActivities: RbacActivity[] = this.getAdminActivities();
    //Remove user management activities,
    this.removeActivity(adminActivities, RbacActivity.READ_ALL_USERS);
    this.removeActivity(adminActivities, RbacActivity.READ_ONE_USER);
    this.removeActivity(adminActivities, RbacActivity.CREATE_USER);
    this.removeActivity(adminActivities, RbacActivity.EDIT_USER);
    this.removeActivity(adminActivities, RbacActivity.DELETE_USER);
    this.removeActivity(adminActivities, RbacActivity.EDIT_LOCKED_TOURNAMENT);
    return adminActivities;
  }

  private getViewerActivities(): RbacActivity[] {
    return [RbacActivity.READ_ALL_TOURNAMENTS,
      RbacActivity.READ_ONE_TOURNAMENT,
      RbacActivity.READ_ALL_TEAMS,
      RbacActivity.READ_ONE_TEAM,
      RbacActivity.READ_ALL_FIGHTS,
      RbacActivity.READ_ONE_FIGHT,
      RbacActivity.READ_ALL_DUELS,
      RbacActivity.READ_ONE_DUEL,
      RbacActivity.READ_ALL_RANKINGS,
      RbacActivity.READ_ONE_RANKING,
      RbacActivity.CHANGE_PASSWORD,
      RbacActivity.CAN_LOGOUT,
      RbacActivity.CHANGE_LANGUAGE,
      RbacActivity.VIEW_TOURNAMENT_STATISTICS,
      RbacActivity.DOWNLOAD_GROUPS_PDF
    ];
  }

  private getGuestActivities(): RbacActivity[] {
    return [];
  }

}
