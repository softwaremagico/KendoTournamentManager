import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Duel} from "../../models/duel";
import {DuelChangedService} from "../../services/notifications/duel-changed.service";
import {KendoComponent} from "../kendo-component";
import {takeUntil} from "rxjs";
import {Tournament} from "../../models/tournament";

@Component({
  selector: 'untie-fight',
  templateUrl: './untie-fight.component.html',
  styleUrls: ['./untie-fight.component.scss']
})
export class UntieFightComponent extends KendoComponent implements OnInit {

  @Input()
  duel: Duel;

  @Input()
  selected: boolean;

  @Input()
  over: boolean;

  @Input()
  swapColors: boolean;

  @Input()
  swapTeams: boolean;

  @Input()
  projectMode: boolean;

  @Output() onSelectedDuel: EventEmitter<any> = new EventEmitter();

  constructor(private duelChangedService: DuelChangedService) {
    super();
  }

  ngOnInit(): void {
    this.duelChangedService.isDuelUpdated.pipe(takeUntil(this.destroySubject)).subscribe(selectedDuel => {
      if (selectedDuel && this.duel) {
        if (selectedDuel.id === this.duel.id) {
          this.selected = true;
        }
      }
    });
  }

  selectDuel(duel: Duel) {
    this.onSelectedDuel.emit([duel]);
  }

  isOver(duel: Duel): boolean {
    return duel.finished;
  }

}
