import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {EnvironmentService} from "../environment.service";
import {MessageService} from "./message.service";
import {LoggerService} from "./logger.service";
import {LoginService} from "./login.service";
import {Observable} from "rxjs";
import {catchError, tap} from "rxjs/operators";
import {Group} from "../models/group";
import {Team} from "../models/team";
import {Duel} from "../models/duel";
import {SystemOverloadService} from "./notifications/system-overload.service";

@Injectable({
  providedIn: 'root'
})
export class GroupService {

  private baseUrl = this.environmentService.getBackendUrl() + '/groups';

  constructor(private http: HttpClient, private environmentService: EnvironmentService, private messageService: MessageService,
              private loggerService: LoggerService, public loginService: LoginService,
              private systemOverloadService: SystemOverloadService) {

  }

  getAll(): Observable<Group[]> {
    const url: string = `${this.baseUrl}`;
    return this.http.get<Group[]>(url, this.loginService.httpOptions)
      .pipe(
        tap({
          next: () => this.loggerService.info(`fetched all groups`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Group[]>(`gets all`))
      );
  }

  update(group: Group): Observable<Group> {
    const url: string = `${this.baseUrl}`;
    return this.http.put<Group>(url, group, this.loginService.httpOptions)
      .pipe(
        tap({
          next: (updatedGroup: Group) => this.loggerService.info(`updating group '${updatedGroup}'`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Group>(`updating '${group}'`))
      );
  }

  getAllByTournament(tournamentId: number): Observable<Group[]> {
    const url: string = `${this.baseUrl}` + '/tournament/' + tournamentId;
    return this.http.get<Group[]>(url, this.loginService.httpOptions)
      .pipe(
        tap({
          next: () => this.loggerService.info(`fetched groups from tournament ${tournamentId}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Group[]>(`gets from tournament ${tournamentId}`))
      );
  }

  setTeamsToGroup(groupId: number, teams: Team[]): Observable<Group> {
    const url: string = `${this.baseUrl}/` + groupId + '/teams';
    return this.http.put<Group>(url, teams, this.loginService.httpOptions)
      .pipe(
        tap({
          next: () => this.loggerService.info(`Updating teams for group ${groupId}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Group>(`updates ${groupId}`))
      );
  }

  addTeamsToGroup(groupId: number, teams: Team[]): Observable<Group> {
    const url: string = `${this.baseUrl}/` + groupId + '/teams/add';
    return this.http.patch<Group>(url, teams, this.loginService.httpOptions)
      .pipe(
        tap({
          next: () => this.loggerService.info(`Adding teams to group ${groupId}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Group>(`updates ${groupId}`))
      );
  }

  deleteTeamsFromGroup(groupId: number, teams: Team[]): Observable<Group> {
    const url: string = `${this.baseUrl}/` + groupId + '/teams/delete';
    return this.http.patch<Group>(url, teams, this.loginService.httpOptions)
      .pipe(
        tap({
          next: () => this.loggerService.info(`Adding teams to group ${groupId}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Group>(`updates ${groupId}`))
      );
  }

  setTeams(teams: Team[]): Observable<Group> {
    const url: string = `${this.baseUrl}/teams`;
    return this.http.put<Group>(url, teams, this.loginService.httpOptions)
      .pipe(
        tap({
          next: () => this.loggerService.info(`Updating teams for default group`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Group>(`updates teams for default group`))
      );
  }

  addUnties(groupId: number, duels: Duel[]): Observable<Group> {
    const url: string = `${this.baseUrl}/` + groupId + `/unties`;
    return this.http.put<Group>(url, duels, this.loginService.httpOptions)
      .pipe(
        tap({
          next: () => this.loggerService.info(`Updating teams for default group`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Group>(`updates teams for default group`))
      );
  }

}
