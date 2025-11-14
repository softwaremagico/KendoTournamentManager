import {Injectable} from "@angular/core";
import {RbacActivity} from "./rbac.activity";
import {UserRoles} from "./user-roles";

@Injectable({
  providedIn: 'root'
})
export class ActivityService {

  activities: RbacActivity[] = [];

  public setRoles(roles: UserRoles[]): void {
    this.activities = this.getActivities(roles);
  }

  public hasRoles(): boolean {
    return this.activities.length > 0;
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
        case 'guest':
          activities = activities.concat(this.getGuestActivities());
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
    this.removeActivity(adminActivities, RbacActivity.LINK_QR_CODE);
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
      RbacActivity.DOWNLOAD_GROUPS_PDF,
      RbacActivity.DOWNLOAD_ALL_FIGHTS,
      RbacActivity.CHECK_TOURNAMENT_BRACKETS,
      RbacActivity.SEE_QR_CODE,
      RbacActivity.DOWNLOAD_QR_CODE,
      RbacActivity.DOWNLOAD_PDF,
      RbacActivity.SHOW_TIMER,
      RbacActivity.PLAY_WHISTLE,
      RbacActivity.EDIT_FIGHT_TIME,
    ];
  }

  private getGuestActivities(): RbacActivity[] {
    return [
      RbacActivity.READ_ONE_TOURNAMENT,
      RbacActivity.READ_ALL_FIGHTS,
      RbacActivity.READ_ONE_FIGHT,
      RbacActivity.READ_ALL_DUELS,
      RbacActivity.READ_ONE_DUEL,
      RbacActivity.CHANGE_LANGUAGE,
      RbacActivity.CHECK_TOURNAMENT_BRACKETS,
      RbacActivity.READ_TEAMS_RANKINGS,
      RbacActivity.READ_COMPETITORS_RANKINGS,
      RbacActivity.CAN_LOGOUT,
      RbacActivity.VIEW_PARTICIPANT_FIGHTS,
      RbacActivity.VIEW_PARTICIPANT_STATISTICS
    ];
  }

}
