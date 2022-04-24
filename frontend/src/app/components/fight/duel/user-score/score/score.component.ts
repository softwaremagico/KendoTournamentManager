import {Component, Input, OnInit} from '@angular/core';
import {FightService} from "../../../../../services/fight.service";
import {Duel} from "../../../../../models/Duel";

@Component({
  selector: 'score',
  templateUrl: './score.component.html',
  styleUrls: ['./score.component.scss']
})
export class ScoreComponent implements OnInit {

  @Input()
  index: number;

  @Input()
  duel: Duel;

  @Input()
  left: boolean;

  constructor(private fightService: FightService) {
  }

  ngOnInit(): void {
  }

}
