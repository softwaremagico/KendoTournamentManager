import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Fight} from "../../models/fight";
import {Duel} from "../../models/duel";
import {DuelChangedService} from "../../services/duel-changed.service";

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

  @Input()
  over: boolean;

  @Output() onSelectedDuel: EventEmitter<any> = new EventEmitter();

  selectedDuel: Duel | undefined;

  constructor(private duelChangedService: DuelChangedService) {
    this.duelChangedService.isDuelSelected.subscribe(selectedDuel => {
      if (selectedDuel && this.fight && this.fight.duels) {
        for (let duel of this.fight.duels) {
          if (selectedDuel.id === duel.id) {
            this.selected = true;
            this.selectedDuel = selectedDuel;
          }
        }
      }
    });
  }

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
    this.onSelectedDuel.emit([duel]);
  }

  isOver(duel: Duel): boolean {
    if (duel.duration) {
      return true;
    }
    return false;
  }

}
