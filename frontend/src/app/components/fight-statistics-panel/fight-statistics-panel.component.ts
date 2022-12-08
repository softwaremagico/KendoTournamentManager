import {Component, OnInit} from '@angular/core';
import {FightStatistics} from "../../models/fight-statistics.model";

@Component({
  selector: 'app-fight-statistics-panel',
  templateUrl: './fight-statistics-panel.component.html',
  styleUrls: ['./fight-statistics-panel.component.scss']
})
export class FightStatisticsPanelComponent implements OnInit {

  fightStatistics: FightStatistics;
  hours: number;
  minutes: number;
  seconds: number;

  constructor(private _fightStatistics: FightStatistics) {
    this.fightStatistics=_fightStatistics;
    this.setHours(_fightStatistics.time);
    this.setMinutes(_fightStatistics.time);
    this.setSeconds(_fightStatistics.time);
  }

  ngOnInit(): void {
  }

  private setHours(seconds: number): void {
    this.hours = seconds / 3600;
  }

  private setMinutes(seconds: number): void {
    this.minutes = (seconds - (seconds / 3600)) / 60;
  }

  private setSeconds(seconds: number): void {
    this.seconds = seconds % 60;
  }

}
