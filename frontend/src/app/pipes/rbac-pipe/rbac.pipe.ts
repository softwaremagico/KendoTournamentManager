import {Pipe, PipeTransform} from '@angular/core';
import {RbacActivity} from "../../services/rbac/rbac.activity";

@Pipe({
  standalone: false,
  name: 'rbac'
})
export class RbacPipe implements PipeTransform {

  transform(activity: RbacActivity | undefined, allowedActivities: RbacActivity[]): boolean {
    if (!activity || !allowedActivities) {
      return false;
    }
    return allowedActivities.includes(activity);
  }

}
