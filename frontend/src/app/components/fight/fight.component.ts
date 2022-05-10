import {Component, Input, OnInit} from '@angular/core';
import {Fight} from "../../models/fight";

@Component({
  selector: 'fight',
  templateUrl: './fight.component.html',
  styleUrls: ['./fight.component.scss']
})
export class FightComponent implements OnInit {

  @Input()
  fight: Fight;

  constructor() {
  }

  ngOnInit(): void {

  }

  showTeamTitle(): boolean {
    if (this.fight?.tournament?.teamSize) {
      return this.fight.tournament.teamSize > 1;
    }
    return true;
  }

}
