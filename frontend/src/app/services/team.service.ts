import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {EnvironmentService} from "../environment.service";
import {AuthenticatedUserService} from "./authenticated-user.service";
import {Observable} from "rxjs";
import {Team} from "../models/team";
import {catchError, tap} from "rxjs/operators";
import {Participant} from "../models/participant";
import {Tournament} from "../models/tournament";
import {MessageService} from "./message.service";
import {LoggerService} from "./logger.service";

@Injectable({
  providedIn: 'root'
})
export class TeamService {

  private baseUrl = this.environmentService.getBackendUrl() + '/teams';

  httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + this.authenticatedUserService.getJwtValue()
    })
  };

  constructor(private http: HttpClient, private environmentService: EnvironmentService, private messageService: MessageService,
              private loggerService: LoggerService, public authenticatedUserService: AuthenticatedUserService) {
  }

  getAll(): Observable<Team[]> {
    const url: string = `${this.baseUrl}/`;
    return this.http.get<Team[]>(url, this.httpOptions)
      .pipe(
        tap(_ => this.loggerService.info(`fetched all teams`)),
        catchError(this.messageService.handleError<Team[]>(`gets all`))
      );
  }

  get(id: number): Observable<Team> {
    const url: string = `${this.baseUrl}/${id}`;
    return this.http.get<Team>(url, this.httpOptions)
      .pipe(
        tap(_ => this.loggerService.info(`fetched team id=${id}`)),
        catchError(this.messageService.handleError<Team>(`get id=${id}`))
      );
  }

  getFromTournament(tournament: Tournament): Observable<Team[]> {
    const url: string = `${this.baseUrl}/tournaments/${tournament.id}`;
    return this.http.get<Team[]>(url, this.httpOptions)
      .pipe(
        tap(_ => this.loggerService.info(`fetched teams from tournament ${tournament.name}`)),
        catchError(this.messageService.handleError<Team[]>(`get from tournament ${tournament}`))
      );
  }

  deleteById(id: number): Observable<number> {
    const url: string = `${this.baseUrl}/${id}`;
    return this.http.delete<number>(url, this.httpOptions)
      .pipe(
        tap(_ => this.loggerService.info(`deleting team id=${id}`)),
        catchError(this.messageService.handleError<number>(`delete id=${id}`))
      );
  }

  delete(team: Team): Observable<Team> {
    const url: string = `${this.baseUrl}/delete`;
    return this.http.post<Team>(url, team, this.httpOptions)
      .pipe(
        tap(_ => this.loggerService.info(`deleting team ${team}`)),
        catchError(this.messageService.handleError<Team>(`delete ${team}`))
      );
  }

  deleteByMemberAndTournament(participant: Participant, tournament: Tournament): Observable<Team> {
    const url: string = `${this.baseUrl}/delete/members`;
    return this.http.post<Team>(url, {participant: participant, tournament: tournament}, this.httpOptions)
      .pipe(
        tap(_ => this.loggerService.info(`deleting member ${participant} on ${tournament}`)),
        catchError(this.messageService.handleError<Team>(`delete member ${participant} on ${tournament}`))
      );
  }

  deleteByMembersAndTournament(participants: Participant[], tournament: Tournament): Observable<Team> {
    const url: string = `${this.baseUrl}/delete/members/all`;
    return this.http.post<Team>(url, {participants: participants, tournament: tournament}, this.httpOptions)
      .pipe(
        tap(_ => this.loggerService.info(`deleting members ${participants} on ${tournament}`)),
        catchError(this.messageService.handleError<Team>(`delete members ${participants} on ${tournament}`))
      );
  }

  deleteByTournament(tournament: Tournament): Observable<Team> {
    const url: string = `${this.baseUrl}/delete/tournaments`;
    return this.http.post<Team>(url, {tournament: tournament}, this.httpOptions)
      .pipe(
        tap(_ => this.loggerService.info(`deleting teams on ${tournament}`)),
        catchError(this.messageService.handleError<Team>(`delete teams on ${tournament}`))
      );
  }

  add(team: Team): Observable<Team> {
    const url: string = `${this.baseUrl}/`;
    return this.http.post<Team>(url, team, this.httpOptions)
      .pipe(
        tap((newTeam: Team) => this.loggerService.info(`adding team ${newTeam.name}`)),
        catchError(this.messageService.handleError<Team>(`adding ${team.name}`))
      );
  }

  update(team: Team): Observable<Team> {
    const url: string = `${this.baseUrl}/`;
    return this.http.put<Team>(url, team, this.httpOptions)
      .pipe(
        tap((updatedTeam: Team) => this.loggerService.info(`updating team ${updatedTeam}`)),
        catchError(this.messageService.handleError<Team>(`updating ${team}`))
      );
  }

}
