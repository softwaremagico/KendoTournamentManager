import {Pipe, PipeTransform} from '@angular/core';
import {RbacActivity} from "../services/rbac/rbac.activity";
import {RbacService} from "../services/rbac/rbac.service";

@Pipe({
  name: 'rbac'
})
export class RbacPipe implements PipeTransform {

  constructor(private rbacService: RbacService) {
  }

  transform(activity: string, ...args: unknown[]): boolean {
    return this.rbacService.isAllowedTo(RbacActivity.getByKey(activity));
  }

}
