import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Duel} from "../../models/duel";
import {DuelChangedService} from "../../services/duel-changed.service";

@Component({
  selector: 'untie-fight',
  templateUrl: './untie-fight.component.html',
  styleUrls: ['./untie-fight.component.scss']
})
export class UntieFightComponent implements OnInit {

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

  @Output() onSelectedDuel: EventEmitter<any> = new EventEmitter();

  constructor(private duelChangedService: DuelChangedService) {
    this.duelChangedService.isDuelSelected.subscribe(selectedDuel => {
      if (selectedDuel && this.duel) {
        if (selectedDuel.id === this.duel.id) {
          this.selected = true;
        }
      }
    });
  }

  ngOnInit(): void {
    // This is intentional
  }

  selectDuel(duel: Duel) {
    this.onSelectedDuel.emit([duel]);
  }

  isOver(duel: Duel): boolean {
    return !!duel.duration;
  }

}
