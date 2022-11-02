import {RbacActivity} from "../services/rbac/rbac.activity";
import {RbacService} from "../services/rbac/rbac.service";

export class RbacBasedComponent {

  constructor(public rbacService: RbacService) {
  }

  get RbacActivity(): typeof RbacActivity {
    return RbacActivity;
  }
}
