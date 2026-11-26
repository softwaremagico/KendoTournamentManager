import {RbacActivity} from "../services/rbac/rbac.activity";
import {RbacService} from "../services/rbac/rbac.service";
import {KendoComponent} from "./kendo-component";

export class RbacBasedComponent extends KendoComponent {

  constructor(public rbacService: RbacService) {
    super();
  }

  get RbacActivity(): typeof RbacActivity {
    return RbacActivity;
  }
}
