import {Component, Input, OnInit} from '@angular/core';
import {Duel} from "../../../models/duel";
import {DuelChangedService} from "../../../services/duel-changed.service";

@Component({
  selector: 'duel',
  templateUrl: './duel.component.html',
  styleUrls: ['./duel.component.scss']
})
export class DuelComponent implements OnInit {

  @Input()
  duel: Duel;

  @Input()
  selected: boolean;

  @Input()
  swapTeams: boolean;

  constructor(private duelChangedService: DuelChangedService) {
    this.duelChangedService.isDuelSelected.subscribe(selectedDuel => {
      if (selectedDuel && this.duel) {
        this.selected = (selectedDuel.id === this.duel.id);
      }
    });
  }

  ngOnInit(): void {
    // This is intentional
  }

}
