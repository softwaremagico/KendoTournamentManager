import {Component, Input} from '@angular/core';
import {Participant} from "../../models/participant";
import {RbacService} from "../../services/rbac/rbac.service";
import {RbacActivity} from "../../services/rbac/rbac.activity";

@Component({
  selector: 'app-user-card',
  templateUrl: './user-card.component.html',
  styleUrls: ['./user-card.component.scss']
})
export class UserCardComponent {

  @Input()
  user: Participant;

  @Input()
  activity: RbacActivity;

  constructor(public rbacService: RbacService) {
  }



}
