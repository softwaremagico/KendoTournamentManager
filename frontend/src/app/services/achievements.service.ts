import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {EnvironmentService} from "../environment.service";
import {MessageService} from "./message.service";
import {LoggerService} from "./logger.service";
import {LoginService} from "./login.service";
import {SystemOverloadService} from "./notifications/system-overload.service";
import {Observable} from "rxjs";
import {catchError, tap} from "rxjs/operators";
import {Achievement} from "../models/achievement.model";

@Injectable({
  providedIn: 'root'
})
export class AchievementsService {

  private baseUrl: string = this.environmentService.getBackendUrl() + '/achievements';

  constructor(private http: HttpClient, private environmentService: EnvironmentService, private messageService: MessageService,
              private loggerService: LoggerService, public loginService: LoginService,
              private systemOverloadService: SystemOverloadService) {
  }

  getParticipantAchievements(participantId: number): Observable<Achievement[]> {
    const url: string = `${this.baseUrl}` + '/participants/' + participantId;
    return this.http.get<Achievement[]>(url)
      .pipe(
        tap({
          next: () => this.loggerService.info(`getting competitors achievements`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Achievement[]>(`getting competitors achievements`))
      );
  }

  getTournamentAchievements(tournamentId: number): Observable<Achievement[]> {
    const url: string = `${this.baseUrl}` + '/tournaments/' + tournamentId;
    return this.http.get<Achievement[]>(url)
      .pipe(
        tap({
          next: () => this.loggerService.info(`getting tournaments achievements`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Achievement[]>(`getting tournaments achievements`))
      );
  }

  regenerateTournamentAchievements(tournamentId: number): Observable<Achievement[]> {
    const url: string = `${this.baseUrl}` + '/tournaments/' + tournamentId;
    return this.http.patch<Achievement[]>(url, null)
      .pipe(
        tap({
          next: () => this.loggerService.info(`generating tournaments achievements`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Achievement[]>(`generating tournaments achievements`))
      );
  }
}
