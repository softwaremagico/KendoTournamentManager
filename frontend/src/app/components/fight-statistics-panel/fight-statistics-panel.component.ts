import {Component, Input, OnInit} from '@angular/core';
import {TournamentFightStatistics} from "../../models/tournament-fight-statistics.model";
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

  @Input()
  teams: boolean;

  fightStatistics: TournamentFightStatistics;
  hours: number;
  minutes: number;
  seconds: number;

  constructor(private statisticsServices: StatisticsService, private statisticsChangedService: StatisticsChangedService) {
    super();
  }

  ngOnInit(): void {
    this.statisticsChangedService.areStatisticsChanged.pipe(takeUntil(this.destroySubject)).subscribe(() => {
      if (this.tournament?.id) {
        this.statisticsServices.getFightStatistics(this.tournament.id, !this.teams, this.teams).subscribe((_fightStatistics) => {
          if (_fightStatistics === undefined || _fightStatistics === null) {
            _fightStatistics = new TournamentFightStatistics();
          }
          this.fightStatistics = _fightStatistics;
          this.setHours(this.fightStatistics.estimatedTime);
          this.setMinutes(this.fightStatistics.estimatedTime);
          this.setSeconds(this.fightStatistics.estimatedTime);
        });
      }
    });
  }

  private setHours(seconds: number): void {
    this.hours = Math.floor(seconds / 3600);
  }

  private setMinutes(seconds: number): void {
    this.minutes = Math.floor((seconds % 3600) / 60);
  }

  private setSeconds(seconds: number): void {
    this.seconds = seconds % 60;
  }

  getTime(): string {
    return (this.hours ? this.toDoubleDigit(this.hours) : "00") + ":" +
      (this.minutes ? this.toDoubleDigit(this.minutes) : "00") + ":" +
      (this.seconds ? this.toDoubleDigit(this.seconds) : "00")
  }

  toDoubleDigit(num: number): string {
    if (isNaN(num)) {
      return '00';
    }
    return num < 10 ? '0' + num : num + '';
  };

}
