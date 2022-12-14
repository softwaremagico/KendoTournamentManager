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

@Injectable({
  providedIn: 'root'
})
export class StatisticsService {

  private baseUrl = this.environmentService.getBackendUrl() + '/statistics';

  constructor(private http: HttpClient, private environmentService: EnvironmentService, private messageService: MessageService,
              private loggerService: LoggerService, public loginService: LoginService,
              private systemOverloadService: SystemOverloadService) {
  }

  get(tournamentId: number, calculateByMembers: boolean, calculateByTeams: boolean): Observable<FightStatistics> {
    let url: string = `${this.baseUrl}/tournament/${tournamentId}`;
    const httpOptions = {
      headers: new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + this.loginService.getJwtValue()
      }),
      params: new HttpParams({
        fromObject : {
          'calculateByMembers' : calculateByMembers,
          'calculateByTeams' : calculateByTeams
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
}
