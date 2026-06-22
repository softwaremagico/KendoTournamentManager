import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Duel} from "../../models/duel";
import {DuelChangedService} from "../../services/notifications/duel-changed.service";
import {KendoComponent} from "../kendo-component";
import {takeUntil} from "rxjs";

@Component({
  selector: 'untie-fight',
  templateUrl: './untie-fight.component.html',
  styleUrls: ['./untie-fight.component.scss']
})
export class UntieFightComponent extends KendoComponent implements OnInit {

  @Input()
  duel: Duel;

  @Input()
  duelSelected: Duel | undefined;

  @Input()
  over: boolean;

  @Input()
  swapColors: boolean;

  @Input()
  swapTeams: boolean;

  @Input()
  projectMode: boolean;

  @Output() selectedDuel: EventEmitter<any> = new EventEmitter();

  constructor(private readonly duelChangedService: DuelChangedService) {
    super();
  }

  ngOnInit(): void {
    this.duelChangedService.isDuelUpdated.pipe(takeUntil(this.destroySubject)).subscribe(selectedDuel => {
      this.duelSelected = selectedDuel;
    });
  }

  selectDuel(duel: Duel) {
    this.selectedDuel.emit([duel]);
  }

  isOver(duel: Duel): boolean {
    return duel.finished;
  }

}
