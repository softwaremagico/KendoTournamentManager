import {Pipe, PipeTransform} from '@angular/core';
import {RbacService} from "../services/rbac/rbac.service";
import {RbacActivity} from "../services/rbac/rbac.activity";
import {ActivityService} from "../services/rbac/activity.service";

@Pipe({
  name: 'hasPermission',
  standalone: true
})
export class HasPermissionPipe implements PipeTransform {
  constructor(private activityService: ActivityService) {
  }

  transform(value: RbacActivity): unknown {
    return this.activityService.isAllowed(value);
  }

}
