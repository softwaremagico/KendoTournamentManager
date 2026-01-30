import {Pipe, PipeTransform} from '@angular/core';
import {RbacActivity} from "../services/rbac/rbac.activity";
import {ActivityService} from "../services/rbac/activity.service";

@Pipe({
  name: 'hasPermission',
  standalone: true
})
export class HasPermissionPipe implements PipeTransform {
  constructor(private activityService: ActivityService) {
  }

  transform(value: RbacActivity): boolean {
    return this.activityService.isAllowed(value);
  }

}
