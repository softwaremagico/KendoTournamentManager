import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {EnvironmentService} from "../environment.service";
import {MessageService} from "./message.service";
import {LoggerService} from "./logger.service";
import {LoginService} from "./login.service";
import {EMPTY, Observable} from "rxjs";
import {catchError, tap} from "rxjs/operators";
import {Group} from "../models/group";
import {Team} from "../models/team";
import {Duel} from "../models/duel";
import {SystemOverloadService} from "./notifications/system-overload.service";

@Injectable({
  providedIn: 'root'
})
export class GroupService {

  private baseUrl: string = this.environmentService.getBackendUrl() + '/groups';

  constructor(private http: HttpClient, private environmentService: EnvironmentService, private messageService: MessageService,
              private loggerService: LoggerService, public loginService: LoginService,
              private systemOverloadService: SystemOverloadService) {

  }

  getAll(): Observable<Group[]> {
    const url: string = `${this.baseUrl}`;
    return this.http.get<Group[]>(url)
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
    return this.http.put<Group>(url, group)
      .pipe(
        tap({
          next: (updatedGroup: Group) => this.loggerService.info(`updating group '${updatedGroup}'`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Group>(`updating '${group}'`))
      );
  }

  getFromTournament(tournamentId: number): Observable<Group[]> {
    const url: string = `${this.baseUrl}/tournaments/${tournamentId}`;
    return this.http.get<Group[]>(url)
      .pipe(
        tap({
          next: () => this.loggerService.info(`fetched groups from tournament ${tournamentId}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Group[]>(`gets from tournament ${tournamentId}`))
      );
  }

  getFromTournamentByIndex(tournamentId: number, level: number, index: number): Observable<Group> {
    const url: string = `${this.baseUrl}/tournaments/${tournamentId}/level/${level}/index/${index}`;
    return this.http.get<Group>(url)
      .pipe(
        tap({
          next: () => this.loggerService.info(`fetched group ${level}-${index} from tournament ${tournamentId}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Group>(`gets group ${level}-${index} from tournament ${tournamentId}`))
      );
  }

  setTeamsToGroup(groupId: number, teams: Team[]): Observable<Group> {
    const url: string = `${this.baseUrl}/${groupId}/teams`;
    return this.http.put<Group>(url, teams)
      .pipe(
        tap({
          next: () => this.loggerService.info(`Updating teams for group ${groupId}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Group>(`updates ${groupId}`))
      );
  }

  deleteTeamsFromTournament(tournamentId: number, teams: Team[]): Observable<Group[]> {
    const url: string = `${this.baseUrl}/tournaments/${tournamentId}/teams/delete`;
    return this.http.patch<Group[]>(url, teams)
      .pipe(
        tap({
          next: () => this.loggerService.info(`Deleting teams from tournament ${tournamentId}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Group[]>(`Deleting teams from tournament ${tournamentId}`))
      );
  }

  deleteAllTeamsFromTournament(tournamentId: number): Observable<Group[]> {
    const url: string = `${this.baseUrl}/tournaments/${tournamentId}/teams/delete`;
    return this.http.delete<Group[]>(url)
      .pipe(
        tap({
          next: () => this.loggerService.info(`Removing all teams from tournament ${tournamentId}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Group[]>(`Removing all teams from tournament ${tournamentId}`))
      );
  }

  addTeamsToGroup(groupId: number, teams: Team[]): Observable<Group> {
    const url: string = `${this.baseUrl}/${groupId}/teams/add`;
    return this.http.patch<Group>(url, teams)
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
    const url: string = `${this.baseUrl}/${groupId}/teams/delete`;
    return this.http.patch<Group>(url, teams)
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
    return this.http.put<Group>(url, teams)
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
    const url: string = `${this.baseUrl}/${groupId}/unties`;
    return this.http.put<Group>(url, duels)
      .pipe(
        tap({
          next: () => this.loggerService.info(`Updating teams for default group`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Group>(`updates teams for default group`))
      );
  }

  addGroup(group: Group): Observable<Group> {
    const url: string = `${this.baseUrl}`;
    return this.http.post<Group>(url, group)
      .pipe(
        tap({
          next: () => this.loggerService.info(`Adding a new group`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Group>(`Adding a new group`))
      );
  }

  deleteGroup(group: Group): Observable<void> {
    if (group) {
      const url: string = `${this.baseUrl}/${group.id}`;
      return this.http.delete<void>(url)
        .pipe(
          tap({
            next: () => this.loggerService.info(`Deleting a group`),
            error: () => this.systemOverloadService.isBusy.next(false),
            complete: () => this.systemOverloadService.isBusy.next(false),
          }),
          catchError(this.messageService.handleError<void>(`Deleting a group`))
        );
    } else {
      return EMPTY;
    }
  }

  getGroupsByTournament(tournamentId: number): Observable<Blob> {
    const url: string = `${this.baseUrl}` + '/tournaments/' + tournamentId + '/pdf';
    return this.http.get<Blob>(url, {
      responseType: 'blob' as 'json', observe: 'body', headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    });
  }

  refreshNonStartedGroups(tournamentId: number, level:number): Observable<void> {
    const url: string = `${this.baseUrl}/refresh/tournaments/${tournamentId}/levels/${level}`;
    return this.http.patch<void>(url, null)
      .pipe(
        tap({
          next: () => this.loggerService.info(`Refreshing groups from tournament ${tournamentId}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<void>(`Refreshing groups from tournament ${tournamentId}`))
      );
  }

}
