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
              public authenticatedUserService: AuthenticatedUserService) {
  }

  getAll(): Observable<Team[]> {
    const url: string = `${this.baseUrl}/`;
    return this.http.get<Team[]>(url, this.httpOptions)
      .pipe(
        tap(_ => this.messageService.log(`fetched all teams`)),
        catchError(this.messageService.handleError<Team[]>(`gets all`))
      );
  }

  get(id: number): Observable<Team> {
    const url: string = `${this.baseUrl}/${id}`;
    return this.http.get<Team>(url, this.httpOptions)
      .pipe(
        tap(_ => this.messageService.log(`fetched team id=${id}`)),
        catchError(this.messageService.handleError<Team>(`get id=${id}`))
      );
  }

  getFromTournament(tournament: Tournament): Observable<Team[]> {
    const url: string = `${this.baseUrl}/tournaments/${tournament.id}`;
    return this.http.get<Team[]>(url, this.httpOptions)
      .pipe(
        tap(_ => this.messageService.log(`fetched teams from tournament ${tournament}`)),
        catchError(this.messageService.handleError<Team[]>(`get from tournament ${tournament}`))
      );
  }

  deleteById(id: number): Observable<number> {
    const url: string = `${this.baseUrl}/${id}`;
    return this.http.delete<number>(url, this.httpOptions)
      .pipe(
        tap(_ => this.messageService.log(`deleting team id=${id}`)),
        catchError(this.messageService.handleError<number>(`delete id=${id}`))
      );
  }

  delete(team: Team): Observable<Team> {
    const url: string = `${this.baseUrl}/delete`;
    return this.http.post<Team>(url, team, this.httpOptions)
      .pipe(
        tap(_ => this.messageService.log(`deleting team ${team}`)),
        catchError(this.messageService.handleError<Team>(`delete ${team}`))
      );
  }

  deleteByMemberAndTournament(participant: Participant, tournament: Tournament): Observable<Team> {
    const url: string = `${this.baseUrl}/delete/members`;
    return this.http.post<Team>(url, {participant: participant, tournament: tournament}, this.httpOptions)
      .pipe(
        tap(_ => this.messageService.log(`deleting member ${participant} on ${tournament}`)),
        catchError(this.messageService.handleError<Team>(`delete member ${participant} on ${tournament}`))
      );
  }

  deleteByTournament(tournament: Tournament): Observable<Team> {
    const url: string = `${this.baseUrl}/delete/tournaments`;
    return this.http.post<Team>(url, {tournament: tournament}, this.httpOptions)
      .pipe(
        tap(_ => this.messageService.log(`deleting teams on ${tournament}`)),
        catchError(this.messageService.handleError<Team>(`delete teams on ${tournament}`))
      );
  }

  add(Team: Team): Observable<Team> {
    const url: string = `${this.baseUrl}/`;
    return this.http.post<Team>(url, Team, this.httpOptions)
      .pipe(
        tap((newTeam: Team) => this.messageService.log(`adding team ${newTeam}`)),
        catchError(this.messageService.handleError<Team>(`adding ${Team}`))
      );
  }

  update(Team: Team): Observable<Team> {
    const url: string = `${this.baseUrl}/`;
    return this.http.put<Team>(url, Team, this.httpOptions)
      .pipe(
        tap((updatedTeam: Team) => this.messageService.log(`updating team ${updatedTeam}`)),
        catchError(this.messageService.handleError<Team>(`updating ${Team}`))
      );
  }

}
