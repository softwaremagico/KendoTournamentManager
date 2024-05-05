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
import {CompetitorRanking} from "../models/competitor-ranking";

@Injectable({
  providedIn: 'root'
})
export class RankingService {

  private baseUrl: string = this.environmentService.getBackendUrl() + '/rankings';

  constructor(private http: HttpClient, private environmentService: EnvironmentService, private messageService: MessageService,
              private loggerService: LoggerService, public loginService: LoginService,
              private systemOverloadService: SystemOverloadService) {
  }

  getCompetitorsScoreRankingByGroup(groupId: number): Observable<ScoreOfCompetitor[]> {
    const url: string = `${this.baseUrl}` + '/competitors/groups/' + groupId;
    return this.http.get<ScoreOfCompetitor[]>(url)
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
    this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}` + '/competitors/tournaments/' + tournamentId;
    return this.http.get<ScoreOfCompetitor[]>(url)
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
    const url: string = `${this.baseUrl}` + '/competitors/tournaments/' + tournamentId + '/pdf';
    return this.http.get<Blob>(url, {
      responseType: 'blob' as 'json', observe: 'body', headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    }).pipe(
      tap({
        next: () => this.loggerService.info(`getting tournament's competitors ranking  as pdf`),
        error: () => this.systemOverloadService.isBusy.next(false),
        complete: () => this.systemOverloadService.isBusy.next(false),
      }),
      catchError(this.messageService.handleError<Blob>(`getting tournament's competitors ranking as pdf`))
    );
  }

  getCompetitorsGlobalScoreRanking(participants: Participant[] | undefined): Observable<ScoreOfCompetitor[]> {
    this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}` + '/competitors';
    return this.http.post<ScoreOfCompetitor[]>(url, participants)
      .pipe(
        tap({
          next: () => this.loggerService.info(`getting competitors ranking`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<ScoreOfCompetitor[]>(`getting competitors ranking`))
      );
  }

  getCompetitorsGlobalScoreRankingAsPdf(participants: Participant[] | undefined): Observable<Blob> {
    this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}` + '/competitors/pdf';
    return this.http.post<Blob>(url, participants, {
      responseType: 'blob' as 'json', observe: 'body', headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    }).pipe(
      tap({
        next: () => this.loggerService.info(`getting competitors ranking as pdf`),
        error: () => this.systemOverloadService.isBusy.next(false),
        complete: () => this.systemOverloadService.isBusy.next(false),
      }),
      catchError(this.messageService.handleError<Blob>(`getting competitors ranking as pdf`))
    );
  }

  getCompetitorsScoreRankingByClub(clubId: number): Observable<ScoreOfCompetitor[]> {
    this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}/competitors/clubs/${clubId}`;
    return this.http.get<ScoreOfCompetitor[]>(url)
      .pipe(
        tap({
          next: () => this.loggerService.info(`getting club's competitors ranking`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<ScoreOfCompetitor[]>(`getting club's competitors ranking`))
      );
  }

  getCompetitorsScoreRankingByClubAsPdf(clubId: number): Observable<Blob> {
    this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}/competitors/clubs/${clubId}/pdf`;
    return this.http.get<Blob>(url, {
      responseType: 'blob' as 'json', observe: 'body', headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    }).pipe(
      tap({
        next: () => this.loggerService.info(`getting club's competitors ranking as pdf`),
        error: () => this.systemOverloadService.isBusy.next(false),
        complete: () => this.systemOverloadService.isBusy.next(false),
      }),
      catchError(this.messageService.handleError<Blob>(`getting club's competitors ranking as pdf`))
    );
  }

  getCompetitorsRanking(participantId: number): Observable<CompetitorRanking> {
    this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}` + `/competitors/${participantId}`;
    return this.http.get<CompetitorRanking>(url)
      .pipe(
        tap({
          next: () => this.loggerService.info(`getting competitor ${participantId} ranking`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<CompetitorRanking>(`getting competitor ${participantId} ranking`))
      );
  }

  getTeamsScoreRankingByGroup(groupId: number): Observable<ScoreOfTeam[]> {
    const url: string = `${this.baseUrl}` + '/teams/groups/' + groupId;
    return this.http.get<ScoreOfTeam[]>(url)
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
    const url: string = `${this.baseUrl}` + '/teams/tournaments/' + tournamentId;
    return this.http.get<ScoreOfTeam[]>(url)
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
    const url: string = `${this.baseUrl}` + '/teams/tournaments/' + tournamentId + '/pdf';
    return this.http.get<Blob>(url, {
      responseType: 'blob' as 'json', observe: 'body', headers: new HttpHeaders({
        'Content-Type': 'application/json'
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

  getTeamsScoreRankingByGroupAsPdf(groupId: number): Observable<Blob> {
    this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}` + '/teams/groups/' + groupId + '/pdf';
    return this.http.get<Blob>(url, {
      responseType: 'blob' as 'json', observe: 'body', headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    }).pipe(
      tap({
        next: () => this.loggerService.info(`getting group's teams ranking`),
        error: () => this.systemOverloadService.isBusy.next(false),
        complete: () => this.systemOverloadService.isBusy.next(false),
      }),
      catchError(this.messageService.handleError<Blob>(`getting group's teams ranking`))
    );
  }

  getTournamentSummaryAsHtml(tournamentId: number): Observable<Blob> {
    this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}` + '/summary/' + tournamentId + '/html';
    return this.http.get<Blob>(url, {
      responseType: 'blob' as 'json', observe: 'body', headers: new HttpHeaders({
        'Content-Type': 'application/json'
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

  getAllListAsZip(tournamentId: number): Observable<Blob> {
    this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}` + '/tournament/' + tournamentId + '/zip';
    return this.http.get<Blob>(url, {
      responseType: 'blob' as 'json', observe: 'body', headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    }).pipe(
      tap({
        next: () => this.loggerService.info(`getting tournament lists as zip`),
        error: () => this.systemOverloadService.isBusy.next(false),
        complete: () => this.systemOverloadService.isBusy.next(false),
      }),
      catchError(this.messageService.handleError<Blob>(`getting tournament lists as zip`))
    );
  }

}
