import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {EnvironmentService} from "../environment.service";
import {LoginService} from "./login.service";
import {Observable} from "rxjs";
import {Team} from "../models/team";
import {catchError, tap} from "rxjs/operators";
import {Participant} from "../models/participant";
import {Tournament} from "../models/tournament";
import {MessageService} from "./message.service";
import {LoggerService} from "./logger.service";
import {SystemOverloadService} from "./notifications/system-overload.service";

@Injectable({
  providedIn: 'root'
})
export class TeamService {

  private baseUrl: string = this.environmentService.getBackendUrl() + '/teams';

  constructor(private http: HttpClient, private environmentService: EnvironmentService, private messageService: MessageService,
              private loggerService: LoggerService, public loginService: LoginService,
              private systemOverloadService: SystemOverloadService) {
  }

  getAll(): Observable<Team[]> {
    const url: string = `${this.baseUrl}`;
    return this.http.get<Team[]>(url)
      .pipe(
        tap({
          next: () => this.loggerService.info(`fetched all teams`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Team[]>(`gets all`))
      );
  }

  get(id: number): Observable<Team> {
    const url: string = `${this.baseUrl}/${id}`;
    return this.http.get<Team>(url)
      .pipe(
        tap({
          next: () => this.loggerService.info(`fetched team id=${id}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Team>(`get id=${id}`))
      );
  }

  getFromTournament(tournament: Tournament): Observable<Team[]> {
    const url: string = `${this.baseUrl}/tournaments/${tournament.id}`;
    return this.http.get<Team[]>(url)
      .pipe(
        tap({
          next: () => this.loggerService.info(`fetched teams from tournament ${tournament.name}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Team[]>(`get from tournament ${tournament}`))
      );
  }

  deleteById(id: number): Observable<number> {
    const url: string = `${this.baseUrl}/${id}`;
    return this.http.delete<number>(url)
      .pipe(
        tap({
          next: () => this.loggerService.info(`deleting team id=${id}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<number>(`delete id=${id}`))
      );
  }

  delete(team: Team): Observable<Team> {
    const url: string = `${this.baseUrl}/delete`;
    return this.http.post<Team>(url, team)
      .pipe(
        tap({
          next: () => this.loggerService.info(`deleting team ${team}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Team>(`delete ${team}`))
      );
  }

  deleteByMemberAndTournament(participant: Participant, tournament: Tournament): Observable<Team> {
    const url: string = `${this.baseUrl}/delete/members`;
    return this.http.post<Team>(url, {
      participant: participant,
      tournament: tournament
    })
      .pipe(
        tap({
          next: () => this.loggerService.info(`deleting member ${participant} on ${tournament}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Team>(`delete member ${participant} on ${tournament}`))
      );
  }

  deleteByMembersAndTournament(participants: Participant[], tournament: Tournament): Observable<Team> {
    const url: string = `${this.baseUrl}/delete/members/all`;
    return this.http.post<Team>(url, {
      participants: participants,
      tournament: tournament
    })
      .pipe(
        tap({
          next: () => this.loggerService.info(`deleting members ${participants} on ${tournament}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Team>(`delete members ${participants} on ${tournament}`))
      );
  }

  deleteByTournament(tournament: Tournament): Observable<Team> {
    const url: string = `${this.baseUrl}/tournaments/delete`;
    return this.http.post<Team>(url, {tournament: tournament})
      .pipe(
        tap({
          next: () => this.loggerService.info(`deleting teams on ${tournament}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Team>(`delete teams on ${tournament}`))
      );
  }

  createByTournament(tournament: Tournament): Observable<Team[]> {
    const url: string = `${this.baseUrl}/tournaments`;
    return this.http.put<Team[]>(url, {tournament: tournament})
      .pipe(
        tap({
          next: () => this.loggerService.info(`creating teams for ${tournament}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Team[]>(`creating teams for ${tournament}`))
      );
  }

  add(team: Team): Observable<Team> {
    const url: string = `${this.baseUrl}`;
    return this.http.post<Team>(url, team)
      .pipe(
        tap({
          next: (newTeam: Team) => this.loggerService.info(`adding team ${newTeam.name}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Team>(`adding ${team.name}`))
      );
  }

  setAll(teams: Team[]): Observable<Team[]> {
    this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}/set`;
    return this.http.post<Team[]>(url, teams)
      .pipe(
        tap({
          next: (newTeam: Team[]) => this.loggerService.info(`adding ` + teams.length + "` teams.`"),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Team[]>(`adding ` + teams.length + "` teams.`"))
      );
  }

  update(team: Team): Observable<Team> {
    const url: string = `${this.baseUrl}`;
    return this.http.put<Team>(url, team)
      .pipe(
        tap({
          next: (updatedTeam: Team) => this.loggerService.info(`updating team ${updatedTeam}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Team>(`updating ${team}`))
      );
  }

  getTeamsByTournament(tournamentId: number): Observable<Blob> {
    const url: string = `${this.baseUrl}` + '/tournaments/' + tournamentId + '/pdf';
    return this.http.get<Blob>(url, {
      responseType: 'blob' as 'json', observe: 'body', headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    });
  }

}
