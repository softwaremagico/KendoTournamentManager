import {Component, OnInit} from '@angular/core';
import {ActivatedRoute, Router} from "@angular/router";
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
import {TranslateService} from "@ngx-translate/core";
import {truncate} from "../../utils/maths/truncate";
import {GaugeChartData} from "../../components/charts/gauge-chart/gauge-chart-data";
import {RankingService} from "../../services/ranking.service";
import {CompetitorRanking} from "../../models/competitor-ranking";
import {AchievementsService} from "../../services/achievements.service";
import {Achievement} from "../../models/achievement.model";
import {ParticipantService} from "../../services/participant.service";
import {Participant} from "../../models/participant";
import {LoginService} from "../../services/login.service";
import {MatDialog} from "@angular/material/dialog";

@Component({
  selector: 'app-participant-statistics',
  templateUrl: './participant-statistics.component.html',
  styleUrls: ['./participant-statistics.component.scss']
})
export class ParticipantStatisticsComponent extends RbacBasedComponent implements OnInit {

  pipe: DatePipe;

  private participantId: number | undefined;
  private temporalToken: string | null;
  public participantStatistics: ParticipantStatistics | undefined = undefined;
  public roleTypes: RoleType[] = RoleType.toArray();
  public competitorRanking: CompetitorRanking;

  public hitsTypeChartData: PieChartData;
  public receivedHitsTypeChartData: PieChartData;
  public performanceRadialData: GaugeChartData;
  public performance: [string, number][];
  public achievements: Achievement[];

  public participant: Participant;

  constructor(private router: Router, private activatedRoute: ActivatedRoute,
              rbacService: RbacService, private systemOverloadService: SystemOverloadService,
              private userSessionService: UserSessionService, private statisticsService: StatisticsService,
              private translateService: TranslateService, private rankingService: RankingService,
              private achievementService: AchievementsService, private participantService: ParticipantService,
              private loginService: LoginService, public dialog: MatDialog) {
    super(rbacService);
    let state = this.router.getCurrentNavigation()?.extras.state;
    if (state) {
      if (state['participantId'] && !isNaN(Number(state['participantId']))) {
        this.participantId = Number(state['participantId']);
      } else {
        this.goBackToUsers();
      }
    } else {
      //Gets participant from URL parameter (from QR codes).
      this.participantId = Number(this.activatedRoute.snapshot.queryParamMap.get('participantId'));
      this.temporalToken = this.activatedRoute.snapshot.queryParamMap.get('temporalToken');
      if (!this.participantId || isNaN(this.participantId)) {
        this.goBackToUsers();
      }
    }
    this.setLocale();
  }

  private setLocale(): void {
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
    if (this.loginService.getJwtValue()) {
      //Already logged in.
      this.initializeData();
    } else {
      if (this.temporalToken) {
        this.loginService.setParticipantUserSession(this.temporalToken, (): void => {
          this.initializeData();
        });
      } else {
        this.goBackToUsers();
      }
    }
  }

  initializeData(): void {
    if (this.participantId) {
      this.participantService.get(this.participantId).subscribe((_participant: Participant): void => {
        this.participant = _participant;
      })
      this.generateStatistics();
    } else {
      //If a participant is logged in directly to this page. Get his id.
      this.participantService.getByUsername().subscribe({
        next: (_participant: Participant): void => {
          this.participantId = _participant.id;
          this.participant = _participant;
          this.generateStatistics();
        },
        error: (): void => {
          console.error("User logged in is not a participant");
          this.goBackToUsers()
        }
      });
    }
  }

  generateStatistics(): void {
    this.systemOverloadService.isTransactionalBusy.next(true);
    this.rankingService.getCompetitorsRanking(this.participantId!).subscribe((_competitorRanking: CompetitorRanking): void => {
      this.competitorRanking = _competitorRanking;
    })
    this.statisticsService.getParticipantStatistics(this.participantId!).subscribe((_participantStatistics: ParticipantStatistics): void => {
      this.participantStatistics = ParticipantStatistics.clone(_participantStatistics);
      this.initializeScoreStatistics(this.participantStatistics);
      this.systemOverloadService.isTransactionalBusy.next(false);
    });
    this.achievementService.getParticipantAchievements(this.participantId!).subscribe((_achievements: Achievement[]): void => {
      this.achievements = _achievements;
    });
  }

  initializeScoreStatistics(participantStatistics: ParticipantStatistics): void {
    this.hitsTypeChartData = PieChartData.fromArray(this.obtainPoints(participantStatistics));
    this.receivedHitsTypeChartData = PieChartData.fromArray(this.obtainReceivedPoints(participantStatistics));
    this.performance = this.generatePerformanceStatistics(participantStatistics);
    this.performanceRadialData = GaugeChartData.fromArray(this.performance);
  }

  generatePerformanceStatistics(participantStatistics: ParticipantStatistics): [string, number][] {
    const performance: [string, number][] = [];
    performance.push(['attack', truncate(participantStatistics.participantFightStatistics.getTotalHits() / (participantStatistics.participantFightStatistics.duelsNumber * 2) * 100, 2)]);
    performance.push(['defense', truncate((1 - (participantStatistics.participantFightStatistics.getTotalReceivedHits() / (participantStatistics.participantFightStatistics.duelsNumber * 2))) * 100, 2)]);
    performance.push(['willpower', participantStatistics.totalTournaments > 0 ?
      (participantStatistics.tournaments / participantStatistics.totalTournaments) * 100 : 0]);
    const aggressivenessMargin: number = 20;
    performance.push(['aggressiveness', participantStatistics.participantFightStatistics.averageWinTime > 0 ?
      Math.min(100, truncate((1 - ((participantStatistics.participantFightStatistics.averageWinTime - aggressivenessMargin) / 180)) * 100, 2)) : 0]);
    performance.push(['affection', participantStatistics.participantFightStatistics.averageLostTime > 0 ?
      Math.min(100, truncate(((participantStatistics.participantFightStatistics.averageLostTime + aggressivenessMargin) / 180) * 100, 2)) : 0]);
    return performance;
  }

  obtainPoints(participantStatistics: ParticipantStatistics): [string, number][] {
    const scores: [string, number][] = [];
    if (participantStatistics?.participantFightStatistics) {
      scores.push([this.translateService.instant(Score.toCamel(Score.MEN)), participantStatistics.participantFightStatistics.menNumber ? participantStatistics.participantFightStatistics.menNumber : 0]);
      scores.push([this.translateService.instant(Score.toCamel(Score.KOTE)), participantStatistics.participantFightStatistics.koteNumber ? participantStatistics.participantFightStatistics.koteNumber : 0]);
      scores.push([this.translateService.instant(Score.toCamel(Score.DO)), participantStatistics.participantFightStatistics.doNumber ? participantStatistics.participantFightStatistics.doNumber : 0]);
      scores.push([this.translateService.instant(Score.toCamel(Score.TSUKI)), participantStatistics.participantFightStatistics.tsukiNumber ? participantStatistics.participantFightStatistics.tsukiNumber : 0]);
      scores.push([this.translateService.instant(Score.toCamel(Score.IPPON)), participantStatistics.participantFightStatistics.ipponNumber ? participantStatistics.participantFightStatistics.ipponNumber : 0]);
      scores.push([this.translateService.instant(Score.toCamel(Score.FUSEN_GACHI)), participantStatistics.participantFightStatistics.fusenGachiNumber ? participantStatistics.participantFightStatistics.fusenGachiNumber : 0]);
      scores.push([this.translateService.instant(Score.toCamel(Score.HANSOKU)), participantStatistics.participantFightStatistics.hansokuNumber ? participantStatistics.participantFightStatistics.hansokuNumber : 0]);
    }
    return scores;
  }

  obtainReceivedPoints(participantStatistics: ParticipantStatistics): [string, number][] {
    const scores: [string, number][] = [];
    if (participantStatistics?.participantFightStatistics) {
      scores.push([this.translateService.instant(Score.toCamel(Score.MEN)), participantStatistics.participantFightStatistics.receivedMenNumber ? participantStatistics.participantFightStatistics.receivedMenNumber : 0]);
      scores.push([this.translateService.instant(Score.toCamel(Score.KOTE)), participantStatistics.participantFightStatistics.receivedKoteNumber ? participantStatistics.participantFightStatistics.receivedKoteNumber : 0]);
      scores.push([this.translateService.instant(Score.toCamel(Score.DO)), participantStatistics.participantFightStatistics.receivedDoNumber ? participantStatistics.participantFightStatistics.receivedDoNumber : 0]);
      scores.push([this.translateService.instant(Score.toCamel(Score.TSUKI)), participantStatistics.participantFightStatistics.receivedTsukiNumber ? participantStatistics.participantFightStatistics.receivedTsukiNumber : 0]);
      scores.push([this.translateService.instant(Score.toCamel(Score.IPPON)), participantStatistics.participantFightStatistics.receivedIpponNumber ? participantStatistics.participantFightStatistics.receivedIpponNumber : 0]);
      scores.push([this.translateService.instant(Score.toCamel(Score.FUSEN_GACHI)), participantStatistics.participantFightStatistics.receivedFusenGachiNumber ? participantStatistics.participantFightStatistics.receivedFusenGachiNumber : 0]);
      scores.push([this.translateService.instant(Score.toCamel(Score.HANSOKU)), participantStatistics.participantFightStatistics.receivedHansokuNumber ? participantStatistics.participantFightStatistics.receivedHansokuNumber : 0]);
    }
    return scores;
  }

  goBackToUsers(): void {
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
