import {Component, Input, OnInit} from '@angular/core';
import {Duel} from "../../../models/Duel";

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

  constructor() {
  }

  ngOnInit(): void {
  }

}
