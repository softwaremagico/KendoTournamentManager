import {Component, Input} from '@angular/core';
import {Participant} from "../../models/participant";

@Component({
  selector: 'app-user-card',
  templateUrl: './user-card.component.html',
  styleUrls: ['./user-card.component.scss']
})
export class UserCardComponent {

  @Input()
  user: Participant;

}
