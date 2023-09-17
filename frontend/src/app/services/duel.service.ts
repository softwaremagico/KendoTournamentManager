import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {EnvironmentService} from "../environment.service";
import {MessageService} from "./message.service";
import {LoggerService} from "./logger.service";
import {LoginService} from "./login.service";
import {Observable} from "rxjs";
import {catchError, tap} from "rxjs/operators";
import {Duel} from "../models/duel";

@Injectable({
  providedIn: 'root'
})
export class DuelService {

  private baseUrl: string = this.environmentService.getBackendUrl() + '/duels';

  constructor(private http: HttpClient, private environmentService: EnvironmentService, private messageService: MessageService,
              private loggerService: LoggerService, public loginService: LoginService) {

  }

  update(duel: Duel): Observable<Duel> {
    const url: string = `${this.baseUrl}`;
    return this.http.put<Duel>(url, duel)
      .pipe(
        tap((_updatedDuel: Duel) => this.loggerService.info(`updating duel`)),
        catchError(this.messageService.handleError<Duel>(`updating duel`))
      );
  }

  getUntiesFromGroup(groupId: number): Observable<Duel[]> {
    const url: string = `${this.baseUrl}/groups/` + groupId + '/unties';
    return this.http.get<Duel[]>(url)
      .pipe(
        tap((_updatedDuel: Duel[]) => this.loggerService.info(`getting unties from group '` + groupId + `'`)),
        catchError(this.messageService.handleError<Duel[]>(`getting unties from group '` + groupId + `'`))
      );
  }

  getUntiesFromTournament(tournamentId: number): Observable<Duel[]> {
    const url: string = `${this.baseUrl}/tournaments/` + tournamentId + '/unties';
    return this.http.get<Duel[]>(url)
      .pipe(
        tap((_updatedDuel: Duel[]) => this.loggerService.info(`getting unties from tournament '` + tournamentId + `'`)),
        catchError(this.messageService.handleError<Duel[]>(`getting unties from tournament '` + tournamentId + `'`))
      );
  }
}
