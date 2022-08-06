import {Component, Input, OnInit} from '@angular/core';
import {Fight} from "../../models/fight";
import {Duel} from "../../models/duel";

@Component({
  selector: 'fight',
  templateUrl: './fight.component.html',
  styleUrls: ['./fight.component.scss']
})
export class FightComponent implements OnInit {

  @Input()
  fight: Fight;

  @Input()
  selected: boolean;

  selectedDuel: Duel | undefined;

  ngOnInit(): void {
    // This is intentional
  }

  showTeamTitle(): boolean {
    if (this.fight?.tournament?.teamSize) {
      return this.fight.tournament.teamSize > 1;
    }
    return true;
  }

  selectDuel(duel: Duel) {
    this.selectedDuel = duel;
  }

}
