import {Component, EventEmitter, Input, Output} from '@angular/core';
import {Team} from "../../models/team";

@Component({
  selector: 'team-card',
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

  @Output()
  onClick: EventEmitter<Team> = new EventEmitter<Team>();

}
