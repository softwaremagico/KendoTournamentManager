import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {EnvironmentService} from "../environment.service";
import {MessageService} from "./message.service";
import {LoggerService} from "./logger.service";
import {AuthenticatedUserService} from "./authenticated-user.service";
import {Observable} from "rxjs";
import {catchError, tap} from "rxjs/operators";
import {ScoreOfTeam} from "../models/score-of-team";
import {ScoreOfCompetitor} from "../models/score-of-competitor";

@Injectable({
  providedIn: 'root'
})
export class RankingService {

  private baseUrl = this.environmentService.getBackendUrl() + '/rankings';

  constructor(private http: HttpClient, private environmentService: EnvironmentService, private messageService: MessageService,
              private loggerService: LoggerService, public authenticatedUserService: AuthenticatedUserService) {
  }

  getCompetitorsScoreRankingByGroup(groupId: number): Observable<ScoreOfCompetitor[]> {
    const url: string = `${this.baseUrl}` + '/competitors/group/' + groupId;
    return this.http.get<ScoreOfCompetitor[]>(url, this.authenticatedUserService.httpOptions)
      .pipe(
        tap(() => this.loggerService.info(`getting competitors ranking`)),
        catchError(this.messageService.handleError<ScoreOfCompetitor[]>(`getting competitors ranking`))
      );
  }

  getCompetitorsScoreRankingByTournament(tournamentId: number): Observable<ScoreOfCompetitor[]> {
    const url: string = `${this.baseUrl}` + '/competitors/tournament/' + tournamentId;
    return this.http.get<ScoreOfCompetitor[]>(url, this.authenticatedUserService.httpOptions)
      .pipe(
        tap(() => this.loggerService.info(`getting competitors ranking`)),
        catchError(this.messageService.handleError<ScoreOfCompetitor[]>(`getting competitors ranking`))
      );
  }

  getCompetitorsScoreRankingByTournamentAsPdf(tournamentId: number): Observable<Blob> {
    const url: string = `${this.baseUrl}` + '/competitors/tournament/' + tournamentId + '/pdf';
    return this.http.get<Blob>(url, {
      responseType: 'blob' as 'json', observe: 'body', headers: new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + this.authenticatedUserService.getJwtValue()
      })
    });
  }

  getTeamsScoreRankingByGroup(groupId: number): Observable<ScoreOfTeam[]> {
    const url: string = `${this.baseUrl}` + '/teams/group/' + groupId;
    return this.http.get<ScoreOfTeam[]>(url, this.authenticatedUserService.httpOptions)
      .pipe(
        tap(() => this.loggerService.info(`getting teams ranking`)),
        catchError(this.messageService.handleError<ScoreOfTeam[]>(`getting teams ranking`))
      );
  }

  getTeamsScoreRankingByTournament(tournamentId: number): Observable<ScoreOfTeam[]> {
    const url: string = `${this.baseUrl}` + '/teams/tournament/' + tournamentId;
    return this.http.get<ScoreOfTeam[]>(url, this.authenticatedUserService.httpOptions)
      .pipe(
        tap(() => this.loggerService.info(`getting teams ranking`)),
        catchError(this.messageService.handleError<ScoreOfTeam[]>(`getting teams ranking`))
      );
  }

  getTeamsScoreRankingByTournamentAsPdf(tournamentId: number): Observable<Blob> {
    const url: string = `${this.baseUrl}` + '/teams/tournament/' + tournamentId + '/pdf';
    return this.http.get<Blob>(url, {
      responseType: 'blob' as 'json', observe: 'body', headers: new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + this.authenticatedUserService.getJwtValue()
      })
    });
  }

}
