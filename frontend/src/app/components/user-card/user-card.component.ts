import {Component, Input} from '@angular/core';
import {Participant} from "../../models/participant";
import {RbacService} from "../../services/rbac/rbac.service";
import {RbacActivity} from "../../services/rbac/rbac.activity";
import {RbacBasedComponent} from "../RbacBasedComponent";

@Component({
  selector: 'user-card',
  templateUrl: './user-card.component.html',
  styleUrls: ['./user-card.component.scss']
})
export class UserCardComponent extends RbacBasedComponent {

  @Input()
  user: Participant;

  @Input()
  activity: RbacActivity = RbacActivity.DRAG_PARTICIPANT;

  @Input()
  dragDisabled: boolean = false;

  @Input()
  showAvatar: boolean = false;

  @Input()
  showClub: boolean = true;

  constructor(rbacService: RbacService) {
    super(rbacService);
  }



}
