import {Injectable} from '@angular/core';
import {Observable} from "rxjs";
import {catchError, tap} from "rxjs/operators";
import {HttpClient} from "@angular/common/http";
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

  get(tournamentId: number): Observable<FightStatistics> {
    const url: string = `${this.baseUrl}/tournament/${tournamentId}`;
    return this.http.get<FightStatistics>(url, this.loginService.httpOptions)
      .pipe(
        tap({
          next: () => this.loggerService.info(`fetched statistics from tournament id=${tournamentId}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<FightStatistics>(`get id=${tournamentId}`))
      );
  }
}
