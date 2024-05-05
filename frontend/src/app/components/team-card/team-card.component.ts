import {Component, Input} from '@angular/core';
import {Team} from "../../models/team";

@Component({
  selector: 'app-team-card',
  templateUrl: './team-card.component.html',
  styleUrls: ['./team-card.component.scss']
})
export class TeamCardComponent {

  @Input()
  team: Team;

  @Input()
  minify: boolean = false;

  @Input()
  disableDrag: boolean = false;

}
