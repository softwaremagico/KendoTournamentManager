import {Component, Input, OnInit} from '@angular/core';
import {FightStatistics} from "../../models/fight-statistics.model";
import {Tournament} from "../../models/tournament";
import {StatisticsService} from "../../services/statistics.service";
import {StatisticsChangedService} from "../../services/notifications/statistics-changed.service";
import {takeUntil} from "rxjs";
import {KendoComponent} from "../kendo-component";

@Component({
  selector: 'app-fight-statistics-panel',
  templateUrl: './fight-statistics-panel.component.html',
  styleUrls: ['./fight-statistics-panel.component.scss']
})
export class FightStatisticsPanelComponent extends KendoComponent implements OnInit {

  @Input()
  tournament: Tournament;

  fightStatistics: FightStatistics;
  hours: number;
  minutes: number;
  seconds: number;

  constructor(private statisticsServices: StatisticsService, private statisticsChangedService: StatisticsChangedService) {
    super();
  }

  ngOnInit(): void {
    this.statisticsChangedService.areStatisticsChanged.pipe(takeUntil(this.destroySubject)).subscribe(() => {
      if (this.tournament && this.tournament.id) {
        this.statisticsServices.get(this.tournament.id).subscribe((_fightStatistics) => {
          this.fightStatistics = _fightStatistics;
          this.setHours(this.fightStatistics.time);
          this.setMinutes(this.fightStatistics.time);
          this.setSeconds(this.fightStatistics.time);
        });
      }
    });
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

  toDoubleDigit(num: number): string {
    if (isNaN(num)) {
      return '00';
    }
    return num < 10 ? '0' + num : num + '';
  };

}
