import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {EnvironmentService} from "../environment.service";
import {MessageService} from "./message.service";
import {LoggerService} from "./logger.service";
import {AuthenticatedUserService} from "./authenticated-user.service";
import {Observable} from "rxjs";
import {catchError, tap} from "rxjs/operators";
import {Group} from "../models/group";
import {Team} from "../models/team";

@Injectable({
  providedIn: 'root'
})
export class GroupService {

  private baseUrl = this.environmentService.getBackendUrl() + '/groups';

  constructor(private http: HttpClient, private environmentService: EnvironmentService, private messageService: MessageService,
              private loggerService: LoggerService, public authenticatedUserService: AuthenticatedUserService) {

  }

  getAll(): Observable<Group[]> {
    const url: string = `${this.baseUrl}`;
    return this.http.get<Group[]>(url, this.authenticatedUserService.httpOptions)
      .pipe(
        tap(_ => this.loggerService.info(`fetched all groups`)),
        catchError(this.messageService.handleError<Group[]>(`gets all`))
      );
  }

  getAllByTournament(tournamentId: number): Observable<Group[]> {
    const url: string = `${this.baseUrl}` + '/tournament/' + tournamentId;
    return this.http.get<Group[]>(url, this.authenticatedUserService.httpOptions)
      .pipe(
        tap(_ => this.loggerService.info(`fetched groups from tournament ${tournamentId}`)),
        catchError(this.messageService.handleError<Group[]>(`gets from tournament ${tournamentId}`))
      );
  }

  setTeamsToGroup(groupId: number, teams: Team[]): Observable<Group> {
    const url: string = `${this.baseUrl}/` + groupId + '/teams';
    return this.http.put<Group>(url, teams, this.authenticatedUserService.httpOptions)
      .pipe(
        tap(_ => this.loggerService.info(`Updating teams for group ${groupId}`)),
        catchError(this.messageService.handleError<Group>(`updates ${groupId}`))
      );
  }

  setTeams(teams: Team[]): Observable<Group> {
    const url: string = `${this.baseUrl}/teams`;
    return this.http.put<Group>(url, teams, this.authenticatedUserService.httpOptions)
      .pipe(
        tap(_ => this.loggerService.info(`Updating teams for default group`)),
        catchError(this.messageService.handleError<Group>(`updates teams for default group`))
      );
  }

}
