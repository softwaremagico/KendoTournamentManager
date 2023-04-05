import {Injectable} from '@angular/core';
import {Observable} from "rxjs";
import {catchError, tap} from "rxjs/operators";
import {HttpClient, HttpHeaders, HttpParams} from "@angular/common/http";
import {EnvironmentService} from "../environment.service";
import {MessageService} from "./message.service";
import {LoggerService} from "./logger.service";
import {LoginService} from "./login.service";
import {SystemOverloadService} from "./notifications/system-overload.service";
import {FightStatistics} from "../models/fight-statistics.model";
import {TournamentStatistics} from "../models/tournament-statistics.model";

@Injectable({
  providedIn: 'root'
})
export class StatisticsService {

  private baseUrl = this.environmentService.getBackendUrl() + '/statistics';

  constructor(private http: HttpClient, private environmentService: EnvironmentService, private messageService: MessageService,
              private loggerService: LoggerService, public loginService: LoginService,
              private systemOverloadService: SystemOverloadService) {
  }

  getFightStatistics(tournamentId: number, calculateByMembers: boolean, calculateByTeams: boolean): Observable<FightStatistics> {
    let url: string = `${this.baseUrl}/tournament/${tournamentId}/fights`;
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + this.loginService.getJwtValue()
      }),
      params: new HttpParams({
        fromObject: {
          'calculateByMembers': calculateByMembers,
          'calculateByTeams': calculateByTeams
        }
      })
    };
    return this.http.get<FightStatistics>(url, httpOptions)
      .pipe(
        tap({
          next: () => this.loggerService.info(`fetched statistics from tournament id=${tournamentId}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.logOnlyError<FightStatistics>(`get id=${tournamentId}`))
      );
  }

  getTournamentStatistics(tournamentId: number): Observable<TournamentStatistics> {
    this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}/tournament/${tournamentId}`;
    return this.http.get<TournamentStatistics>(url, this.loginService.httpOptions)
      .pipe(
        tap({
          next: () => this.loggerService.info(`fetched statistics from tournament id=${tournamentId}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.logOnlyError<TournamentStatistics>(`get id=${tournamentId}`))
      );
  }

  getPreviousTournamentStatistics(tournamentId: number): Observable<TournamentStatistics[]> {
    //this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}/tournament/${tournamentId}/previous/10`;
    return this.http.get<TournamentStatistics[]>(url, this.loginService.httpOptions)
      .pipe(
        tap({
          next: () => this.loggerService.info(`fetched statistics from tournament id=${tournamentId}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.logOnlyError<TournamentStatistics[]>(`get id=${tournamentId}`))
      );
  }
}
