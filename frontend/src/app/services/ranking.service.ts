import {ServiceRequestMonitor} from './service-request-monitor';
import {Injectable} from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from "@angular/common/http";
import {EnvironmentService} from "../environment.service";
import {MessageService} from "./message.service";
import {LoggerService} from "./logger.service";
import {LoginService} from "./login.service";
import {Observable} from "rxjs";
import {ScoreOfTeam} from "../models/score-of-team";
import {ScoreOfCompetitor} from "../models/score-of-competitor";
import {SystemOverloadService} from "./notifications/system-overload.service";
import {Participant} from "../models/participant";
import {CompetitorRanking} from "../models/competitor-ranking";

/**
 * Angular service for competitor and team ranking data.
 *
 * Wraps the backend {@code /rankings} REST endpoints. Provides:
 * - Per-group and per-tournament competitor score rankings.
 * - Global (cross-tournament) competitor score rankings.
 * - Team score rankings per group and per tournament.
 * - PDF export of rankings.
 * - A custom global ranking computed from a list of participants and a time window.
 */
@Injectable({
  providedIn: 'root'
})
export class RankingService {
  private readonly monitor = new ServiceRequestMonitor(this.loggerService, this.messageService, this.systemOverloadService);


  private readonly baseUrl: string = this.environmentService.getBackendUrl() + '/rankings';

  constructor(private readonly http: HttpClient, private readonly environmentService: EnvironmentService, private readonly messageService: MessageService,
              private readonly loggerService: LoggerService, public readonly loginService: LoginService,
              private readonly systemOverloadService: SystemOverloadService) {
  }

  getCompetitorsScoreRankingByGroup(groupId: number): Observable<ScoreOfCompetitor[]> {
    const url: string = `${this.baseUrl}` + '/competitors/groups/' + groupId;
    return this.http.get<ScoreOfCompetitor[]>(url)
      .pipe(this.monitor.track<ScoreOfCompetitor[]>(`getting competitors ranking`, () => `getting competitors ranking`));
  }

  getCompetitorsScoreRankingByTournament(tournamentId: number): Observable<ScoreOfCompetitor[]> {
    this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}` + '/competitors/tournaments/' + tournamentId;
    return this.http.get<ScoreOfCompetitor[]>(url)
      .pipe(this.monitor.track<ScoreOfCompetitor[]>(`getting competitors ranking`, () => `getting competitors ranking`));
  }

  getCompetitorsScoreRankingByTournamentAsPdf(tournamentId: number): Observable<Blob> {
    this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}` + '/competitors/tournaments/' + tournamentId + '/pdf';
    return this.http.get<Blob>(url, {
      responseType: 'blob' as 'json', observe: 'body', headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    }).pipe(this.monitor.track<Blob>(`getting tournament's competitors ranking as pdf`, () => `getting tournament's competitors ranking  as pdf`));
  }

  getCompetitorsGlobalScoreRanking(participants: (Participant | undefined)[] | undefined, fromDays: number | undefined): Observable<ScoreOfCompetitor[]> {
    this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}` + '/competitors';
    return this.http.post<ScoreOfCompetitor[]>(url, participants, {
      params: new HttpParams({
        fromObject: {
          'from': fromDays ?? 0,
        }
      })
    })
      .pipe(this.monitor.track<ScoreOfCompetitor[]>(`getting competitors ranking`, () => `getting competitors ranking`));
  }

  getCompetitorsGlobalScoreRankingAsPdf(participants: Participant[] | undefined, fromDays: number | undefined): Observable<Blob> {
    this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}` + '/competitors/pdf';

    const httpOptions = {
      responseType: 'blob' as 'json',
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      }),
      params: new HttpParams({
        fromObject: {
          'from': fromDays ?? 0,
        }
      })
    };

    return this.http.post<Blob>(url, participants, httpOptions)
      .pipe(this.monitor.track<Blob>(`getting competitors ranking as pdf`, () => `getting competitors ranking as pdf`));
  }

  getCompetitorsScoreRankingByClub(clubId: number): Observable<ScoreOfCompetitor[]> {
    this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}/competitors/clubs/${clubId}`;
    return this.http.get<ScoreOfCompetitor[]>(url)
      .pipe(this.monitor.track<ScoreOfCompetitor[]>(`getting club's competitors ranking`, () => `getting club's competitors ranking`));
  }

  getCompetitorsScoreRankingByClubAsPdf(clubId: number): Observable<Blob> {
    this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}/competitors/clubs/${clubId}/pdf`;
    return this.http.get<Blob>(url, {
      responseType: 'blob' as 'json', observe: 'body', headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    }).pipe(this.monitor.track<Blob>(`getting club's competitors ranking as pdf`, () => `getting club's competitors ranking as pdf`));
  }

  getCompetitorsRanking(participantId: number): Observable<CompetitorRanking> {
    this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}` + `/competitors/${participantId}`;
    return this.http.get<CompetitorRanking>(url)
      .pipe(this.monitor.track<CompetitorRanking>(`getting competitor ${participantId} ranking`, () => `getting competitor ${participantId} ranking`));
  }

  getTeamsScoreRankingByGroup(groupId: number): Observable<ScoreOfTeam[]> {
    const url: string = `${this.baseUrl}` + '/teams/groups/' + groupId;
    return this.http.get<ScoreOfTeam[]>(url)
      .pipe(this.monitor.track<ScoreOfTeam[]>(`getting teams ranking`, () => `getting teams ranking`));
  }

  getTeamsScoreRankingByTournament(tournamentId: number): Observable<ScoreOfTeam[]> {
    const url: string = `${this.baseUrl}` + '/teams/tournaments/' + tournamentId;
    return this.http.get<ScoreOfTeam[]>(url)
      .pipe(this.monitor.track<ScoreOfTeam[]>(`getting teams ranking`, () => `getting teams ranking`));
  }

  getTeamsScoreRankingByTournamentAsPdf(tournamentId: number): Observable<Blob> {
    this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}` + '/teams/tournaments/' + tournamentId + '/pdf';
    return this.http.get<Blob>(url, {
      responseType: 'blob' as 'json', observe: 'body', headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    }).pipe(this.monitor.track<Blob>(`getting teams ranking`, () => `getting teams ranking`));
  }

  getTeamsScoreRankingByGroupAsPdf(groupId: number): Observable<Blob> {
    this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}` + '/teams/groups/' + groupId + '/pdf';
    return this.http.get<Blob>(url, {
      responseType: 'blob' as 'json', observe: 'body', headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    }).pipe(this.monitor.track<Blob>(`getting group's teams ranking`, () => `getting group's teams ranking`));
  }

  getTournamentSummaryAsHtml(tournamentId: number): Observable<Blob> {
    this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}` + '/summary/' + tournamentId + '/html';
    return this.http.get<Blob>(url, {
      responseType: 'blob' as 'json', observe: 'body', headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    }).pipe(this.monitor.track<Blob>(`getting tournament summary code`, () => `getting tournament summary code`));
  }

  getAllListAsZip(tournamentId: number): Observable<Blob> {
    this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}` + '/tournament/' + tournamentId + '/zip';
    return this.http.get<Blob>(url, {
      responseType: 'blob' as 'json', observe: 'body', headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    }).pipe(this.monitor.track<Blob>(`getting tournament lists as zip`, () => `getting tournament lists as zip`));
  }

}
