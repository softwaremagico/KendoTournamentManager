import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Fight} from "../../models/fight";
import {Duel} from "../../models/duel";
import {DuelChangedService} from "../../services/duel-changed.service";
import {KendoComponent} from "../kendo-component";
import {takeUntil} from "rxjs";

@Component({
  selector: 'fight',
  templateUrl: './fight.component.html',
  styleUrls: ['./fight.component.scss']
})
export class FightComponent extends KendoComponent implements OnInit {

  @Input()
  fight: Fight;

  @Input()
  selected: boolean;

  @Input()
  over: boolean;

  @Output() onSelectedDuel: EventEmitter<any> = new EventEmitter();

  selectedDuel: Duel | undefined;

  @Input()
  swapColors: boolean;

  @Input()
  swapTeams: boolean;

  constructor(private duelChangedService: DuelChangedService) {
    super();
  }

  ngOnInit(): void {
    this.duelChangedService.isDuelUpdated.pipe(takeUntil(this.destroySubject)).subscribe(selectedDuel => {
      if (selectedDuel && this.fight && this.fight.duels) {
        this.selected = false;
        this.selectedDuel = undefined;
        for (let duel of this.fight.duels) {
          if (selectedDuel === duel) {
            this.selected = true;
            this.selectedDuel = selectedDuel;
          }
        }
      }
    });
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
    return !!duel.duration;
  }

}
