import {Component, Input, OnInit} from '@angular/core';
import {Duel} from "../../../../models/duel";
import {DuelType} from "../../../../models/duel-type";

@Component({
  selector: 'user-score',
  templateUrl: './user-score.component.html',
  styleUrls: ['./user-score.component.scss']
})
export class UserScoreComponent implements OnInit {

  @Input()
  duel: Duel;

  @Input()
  left: boolean;

  ngOnInit(): void {
    // This is intentional
  }

  isUntie(): boolean {
    return this.duel !== undefined && this.duel.type === DuelType.UNDRAW;
  }

}
