import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {EnvironmentService} from "../environment.service";
import {MessageService} from "./message.service";
import {LoggerService} from "./logger.service";
import {LoginService} from "./login.service";
import {Observable} from "rxjs";
import {catchError, tap} from "rxjs/operators";
import {ScoreOfTeam} from "../models/score-of-team";
import {ScoreOfCompetitor} from "../models/score-of-competitor";
import {SystemOverloadService} from "./notifications/system-overload.service";
import {Participant} from "../models/participant";

@Injectable({
  providedIn: 'root'
})
export class RankingService {

  private baseUrl = this.environmentService.getBackendUrl() + '/rankings';

  constructor(private http: HttpClient, private environmentService: EnvironmentService, private messageService: MessageService,
              private loggerService: LoggerService, public loginService: LoginService,
              private systemOverloadService: SystemOverloadService) {
  }

  getCompetitorsScoreRankingByGroup(groupId: number): Observable<ScoreOfCompetitor[]> {
    const url: string = `${this.baseUrl}` + '/competitors/group/' + groupId;
    return this.http.get<ScoreOfCompetitor[]>(url, this.loginService.httpOptions)
      .pipe(
        tap({
          next: () => this.loggerService.info(`getting competitors ranking`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<ScoreOfCompetitor[]>(`getting competitors ranking`))
      );
  }

  getCompetitorsScoreRankingByTournament(tournamentId: number): Observable<ScoreOfCompetitor[]> {
    const url: string = `${this.baseUrl}` + '/competitors/tournament/' + tournamentId;
    return this.http.get<ScoreOfCompetitor[]>(url, this.loginService.httpOptions)
      .pipe(
        tap({
          next: () => this.loggerService.info(`getting competitors ranking`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<ScoreOfCompetitor[]>(`getting competitors ranking`))
      );
  }

  getCompetitorsGlobalScoreRanking(participants: Participant[]): Observable<ScoreOfCompetitor[]> {
    this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}` + '/competitors';
    return this.http.post<ScoreOfCompetitor[]>(url, participants, this.loginService.httpOptions)
      .pipe(
        tap({
          next: () => this.loggerService.info(`getting competitors ranking`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<ScoreOfCompetitor[]>(`getting competitors ranking`))
      );
  }

  getCompetitorsScoreRankingByTournamentAsPdf(tournamentId: number): Observable<Blob> {
    this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}` + '/competitors/tournament/' + tournamentId + '/pdf';
    return this.http.get<Blob>(url, {
      responseType: 'blob' as 'json', observe: 'body', headers: new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + this.loginService.getJwtValue()
      })
    }).pipe(
      tap({
        next: () => this.loggerService.info(`getting competitors ranking`),
        error: () => this.systemOverloadService.isBusy.next(false),
        complete: () => this.systemOverloadService.isBusy.next(false),
      }),
      catchError(this.messageService.handleError<Blob>(`getting competitors ranking`))
    );
  }

  getTeamsScoreRankingByGroup(groupId: number): Observable<ScoreOfTeam[]> {
    const url: string = `${this.baseUrl}` + '/teams/group/' + groupId;
    return this.http.get<ScoreOfTeam[]>(url, this.loginService.httpOptions)
      .pipe(
        tap({
          next: () => this.loggerService.info(`getting teams ranking`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<ScoreOfTeam[]>(`getting teams ranking`))
      );
  }

  getTeamsScoreRankingByTournament(tournamentId: number): Observable<ScoreOfTeam[]> {
    const url: string = `${this.baseUrl}` + '/teams/tournament/' + tournamentId;
    return this.http.get<ScoreOfTeam[]>(url, this.loginService.httpOptions)
      .pipe(
        tap({
          next: () => this.loggerService.info(`getting teams ranking`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<ScoreOfTeam[]>(`getting teams ranking`))
      );
  }

  getTeamsScoreRankingByTournamentAsPdf(tournamentId: number): Observable<Blob> {
    this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}` + '/teams/tournament/' + tournamentId + '/pdf';
    return this.http.get<Blob>(url, {
      responseType: 'blob' as 'json', observe: 'body', headers: new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + this.loginService.getJwtValue()
      })
    }).pipe(
      tap({
        next: () => this.loggerService.info(`getting teams ranking`),
        error: () => this.systemOverloadService.isBusy.next(false),
        complete: () => this.systemOverloadService.isBusy.next(false),
      }),
      catchError(this.messageService.handleError<Blob>(`getting teams ranking`))
    );
  }

  getTournamentSummaryAsHtml(tournamentId: number): Observable<Blob> {
    this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}` + '/summary/' + tournamentId + '/html';
    return this.http.get<Blob>(url, {
      responseType: 'blob' as 'json', observe: 'body', headers: new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + this.loginService.getJwtValue()
      })
    }).pipe(
      tap({
        next: () => this.loggerService.info(`getting tournament summary code`),
        error: () => this.systemOverloadService.isBusy.next(false),
        complete: () => this.systemOverloadService.isBusy.next(false),
      }),
      catchError(this.messageService.handleError<Blob>(`getting tournament summary code`))
    );
  }

}
