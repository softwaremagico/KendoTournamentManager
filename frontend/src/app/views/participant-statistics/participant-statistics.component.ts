import {Component, OnInit} from '@angular/core';
import {Router} from "@angular/router";
import {RbacService} from "../../services/rbac/rbac.service";
import {SystemOverloadService} from "../../services/notifications/system-overload.service";
import {RbacBasedComponent} from "../../components/RbacBasedComponent";
import {ParticipantStatistics} from "../../models/participant-statistics.model";
import {DatePipe} from "@angular/common";
import {UserSessionService} from "../../services/user-session.service";
import {RoleType} from "../../models/role-type";
import {convertDate, convertSeconds} from "../../utils/dates/date-conversor";
import {StatisticsService} from "../../services/statistics.service";
import {PieChartData} from "../../components/charts/pie-chart/pie-chart-data";
import {Score} from "../../models/score";

@Component({
  selector: 'app-participant-statistics',
  templateUrl: './participant-statistics.component.html',
  styleUrls: ['./participant-statistics.component.scss']
})
export class ParticipantStatisticsComponent extends RbacBasedComponent implements OnInit {

  pipe: DatePipe;

  private readonly participantId: number | undefined;
  public participantStatistics: ParticipantStatistics | undefined = undefined;
  public roleTypes: RoleType[] = RoleType.toArray();

  public hitsTypeChartData: PieChartData;
  public receivedHitsTypeChartData: PieChartData;

  constructor(private router: Router, rbacService: RbacService, private systemOverloadService: SystemOverloadService,
              private userSessionService: UserSessionService, private statisticsService: StatisticsService,) {
    super(rbacService);
    let state = this.router.getCurrentNavigation()?.extras.state;
    if (state) {
      if (state['participantId'] && !isNaN(Number(state['participantId']))) {
        this.participantId = Number(state['participantId']);
      } else {
        this.goBackToUsers();
      }
    } else {
      this.goBackToUsers();
    }
    this.setLocale();
  }

  private setLocale() {
    if (this.userSessionService.getLanguage() === 'es' || this.userSessionService.getLanguage() === 'ca') {
      this.pipe = new DatePipe('es');
    } else if (this.userSessionService.getLanguage() === 'it') {
      this.pipe = new DatePipe('it');
    } else if (this.userSessionService.getLanguage() === 'de') {
      this.pipe = new DatePipe('de');
    } else if (this.userSessionService.getLanguage() === 'nl') {
      this.pipe = new DatePipe('nl');
    } else {
      this.pipe = new DatePipe('en-US');
    }
  }

  ngOnInit(): void {
    this.generateStatistics();
  }

  generateStatistics(): void {
    this.systemOverloadService.isTransactionalBusy.next(true);
    this.statisticsService.getParticipantStatistics(this.participantId!).subscribe((participantStatistics: ParticipantStatistics) => {
      this.participantStatistics = ParticipantStatistics.clone(participantStatistics);
      this.initializeScoreStatistics(this.participantStatistics);
      this.systemOverloadService.isTransactionalBusy.next(false);
    });
  }

  initializeScoreStatistics(participantStatistics: ParticipantStatistics): void {
    this.hitsTypeChartData = PieChartData.fromArray(this.obtainPoints(participantStatistics));
    this.receivedHitsTypeChartData = PieChartData.fromArray(this.obtainReceivedPoints(participantStatistics));
  }

  obtainPoints(participantStatistics: ParticipantStatistics): [string, number][] {
    const scores: [string, number][] = [];
    if (participantStatistics && participantStatistics.participantFightStatistics) {
      scores.push([Score.label(Score.MEN), participantStatistics.participantFightStatistics.menNumber ? participantStatistics.participantFightStatistics.menNumber : 0]);
      scores.push([Score.label(Score.KOTE), participantStatistics.participantFightStatistics.koteNumber ? participantStatistics.participantFightStatistics.koteNumber : 0]);
      scores.push([Score.label(Score.DO), participantStatistics.participantFightStatistics.doNumber ? participantStatistics.participantFightStatistics.doNumber : 0]);
      scores.push([Score.label(Score.TSUKI), participantStatistics.participantFightStatistics.tsukiNumber ? participantStatistics.participantFightStatistics.tsukiNumber : 0]);
      scores.push([Score.label(Score.IPPON), participantStatistics.participantFightStatistics.ipponNumber ? participantStatistics.participantFightStatistics.ipponNumber : 0]);
    }
    return scores;
  }

  obtainReceivedPoints(participantStatistics: ParticipantStatistics): [string, number][] {
    const scores: [string, number][] = [];
    if (participantStatistics && participantStatistics.participantFightStatistics) {
      scores.push([Score.label(Score.MEN), participantStatistics.participantFightStatistics.receivedMenNumber ? participantStatistics.participantFightStatistics.receivedMenNumber : 0]);
      scores.push([Score.label(Score.KOTE), participantStatistics.participantFightStatistics.receivedKoteNumber ? participantStatistics.participantFightStatistics.receivedKoteNumber : 0]);
      scores.push([Score.label(Score.DO), participantStatistics.participantFightStatistics.receivedDoNumber ? participantStatistics.participantFightStatistics.receivedDoNumber : 0]);
      scores.push([Score.label(Score.TSUKI), participantStatistics.participantFightStatistics.receivedTsukiNumber ? participantStatistics.participantFightStatistics.receivedTsukiNumber : 0]);
      scores.push([Score.label(Score.IPPON), participantStatistics.participantFightStatistics.receivedIpponNumber ? participantStatistics.participantFightStatistics.receivedIpponNumber : 0]);
    }
    return scores;
  }

  goBackToUsers() {
    this.router.navigate(['/participants'], {});
  }

  numberOfPerformedRoles(roleType: RoleType): number {
    if (this.participantStatistics !== undefined) {
      return this.participantStatistics.numberOfRolePerformed(roleType);
    }
    return 0;
  }

  convertSeconds(seconds: number | undefined): string {
    return convertSeconds(seconds);
  }

  convertDate(date: Date | undefined): string | null {
    return convertDate(this.pipe, date);
  }
}
