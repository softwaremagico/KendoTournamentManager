import {Pipe, PipeTransform} from '@angular/core';
import {RbacActivity} from "../../services/rbac/rbac.activity";

@Pipe({
  name: 'rbac'
})
export class RbacPipe implements PipeTransform {

  constructor() {
  }

  transform(activity: RbacActivity, allowedActivities: RbacActivity[]): boolean {
    if (!allowedActivities) {
      return false;
    }
    return allowedActivities.includes(activity);
  }

}
