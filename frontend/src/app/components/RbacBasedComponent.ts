import {RbacActivity} from "../services/rbac/rbac.activity";

export class RbacBasedComponent {

  get RbacActivity(): typeof RbacActivity {
    return RbacActivity;
  }
}
