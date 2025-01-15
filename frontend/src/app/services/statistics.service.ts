import {Injectable} from '@angular/core';
import {Observable} from "rxjs";
import {catchError, tap} from "rxjs/operators";
import {HttpClient, HttpHeaders, HttpParams} from "@angular/common/http";
import {EnvironmentService} from "../environment.service";
import {MessageService} from "./message.service";
import {LoggerService} from "./logger.service";
import {LoginService} from "./login.service";
import {SystemOverloadService} from "./notifications/system-overload.service";
import {TournamentFightStatistics} from "../models/tournament-fight-statistics.model";
import {TournamentStatistics} from "../models/tournament-statistics.model";
import {ParticipantStatistics} from "../models/participant-statistics.model";
import {Participant} from "../models/participant";

@Injectable({
  providedIn: 'root'
})
export class StatisticsService {

  private baseUrl: string = this.environmentService.getBackendUrl() + '/statistics';

  constructor(private http: HttpClient, private environmentService: EnvironmentService, private messageService: MessageService,
              private loggerService: LoggerService, public loginService: LoginService,
              private systemOverloadService: SystemOverloadService) {
  }

  getFightStatistics(tournamentId: number, calculateByMembers: boolean, calculateByTeams: boolean): Observable<TournamentFightStatistics> {
    let url: string = `${this.baseUrl}/tournaments/${tournamentId}/fights`;
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json'
      }),
      params: new HttpParams({
        fromObject: {
          'calculateByMembers': calculateByMembers,
          'calculateByTeams': calculateByTeams
        }
      })
    };
    return this.http.get<TournamentFightStatistics>(url, httpOptions)
      .pipe(
        tap({
          next: () => this.loggerService.info(`fetched statistics from tournament id=${tournamentId}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.logOnlyError<TournamentFightStatistics>(`get id=${tournamentId}`))
      );
  }

  getTournamentStatistics(tournamentId: number): Observable<TournamentStatistics> {
    this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}/tournaments/${tournamentId}`;
    return this.http.get<TournamentStatistics>(url)
      .pipe(
        tap({
          next: () => this.loggerService.info(`fetched statistics from tournament id=${tournamentId}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.logOnlyError<TournamentStatistics>(`get id=${tournamentId}`))
      );
  }

  getPreviousTournamentStatistics(tournamentId: number, tournamentsToRetrieve: number): Observable<TournamentStatistics[]> {
    const url: string = `${this.baseUrl}/tournaments/${tournamentId}/previous/${tournamentsToRetrieve}`;
    return this.http.get<TournamentStatistics[]>(url)
      .pipe(
        tap({
          next: () => this.loggerService.info(`fetched statistics from tournament id=${tournamentId}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.logOnlyError<TournamentStatistics[]>(`get id=${tournamentId}`))
      );
  }

  getParticipantStatistics(participantId: number): Observable<ParticipantStatistics> {
    this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}/participants/${participantId}`;
    return this.http.get<ParticipantStatistics>(url)
      .pipe(
        tap({
          next: () => this.loggerService.info(`fetched statistics from participant id=${participantId}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.logOnlyError<ParticipantStatistics>(`get id=${participantId}`))
      );
  }


  getYourWorstNightmare(participantId: number): Observable<Participant[]> {
    const url: string = `${this.baseUrl}/participants/your-worst-nightmare/${participantId}`;
    return this.http.get<Participant[]>(url)
      .pipe(
        tap({
          next: () => this.loggerService.info(`fetched worst nightmare for participant id=${participantId}`)
        }),
        catchError(this.messageService.logOnlyError<Participant[]>(`get id=${participantId}`))
      );
  }


  getWorstNightmareOf(participantId: number): Observable<Participant[]> {
    const url: string = `${this.baseUrl}/participants/worst-nightmare-of/${participantId}`;
    return this.http.get<Participant[]>(url)
      .pipe(
        tap({
          next: () => this.loggerService.info(`fetched worst nightmare to participant id=${participantId}`),
        }),
        catchError(this.messageService.logOnlyError<Participant[]>(`get id=${participantId}`))
      );
  }
}
