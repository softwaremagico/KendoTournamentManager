import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {EnvironmentService} from "../environment.service";
import {LoggerService} from "../logger.service";
import {AuthenticatedUserService} from "./authenticated-user.service";
import {Observable, of} from "rxjs";
import {Team} from "../models/team";
import {catchError, tap} from "rxjs/operators";
import {Participant} from "../models/participant";
import {Tournament} from "../models/tournament";

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

  constructor(private http: HttpClient, private environmentService: EnvironmentService, private loggerService: LoggerService,
              public authenticatedUserService: AuthenticatedUserService) {
  }

  getAll(): Observable<Team[]> {
    const url: string = `${this.baseUrl}/`;
    return this.http.get<Team[]>(url, this.httpOptions)
      .pipe(
        tap(_ => this.log(`fetched all teams`)),
        catchError(this.handleError<Team[]>(`gets all`))
      );
  }

  get(id: number): Observable<Team> {
    const url: string = `${this.baseUrl}/${id}`;
    return this.http.get<Team>(url, this.httpOptions)
      .pipe(
        tap(_ => this.log(`fetched team id=${id}`)),
        catchError(this.handleError<Team>(`get id=${id}`))
      );
  }

  getFromTournament(tournament: Tournament): Observable<Team[]> {
    const url: string = `${this.baseUrl}/tournaments/${tournament.id}`;
    return this.http.get<Team[]>(url, this.httpOptions)
      .pipe(
        tap(_ => this.log(`fetched teams from tournament ${tournament}`)),
        catchError(this.handleError<Team[]>(`get from tournament ${tournament}`))
      );
  }

  deleteById(id: number): Observable<number> {
    const url: string = `${this.baseUrl}/${id}`;
    return this.http.delete<number>(url, this.httpOptions)
      .pipe(
        tap(_ => this.log(`deleting team id=${id}`)),
        catchError(this.handleError<number>(`delete id=${id}`))
      );
  }

  delete(team: Team): Observable<Team> {
    const url: string = `${this.baseUrl}/delete`;
    return this.http.post<Team>(url, team, this.httpOptions)
      .pipe(
        tap(_ => this.log(`deleting team ${team}`)),
        catchError(this.handleError<Team>(`delete ${team}`))
      );
  }

  deleteByTournament(tournament: Tournament): Observable<Team> {
    const url: string = `${this.baseUrl}/delete/tournaments`;
    return this.http.post<Team>(url, {tournament: tournament}, this.httpOptions)
      .pipe(
        tap(_ => this.log(`deleting teams on ${tournament}`)),
        catchError(this.handleError<Team>(`delete teams on ${tournament}`))
      );
  }

  add(Team: Team): Observable<Team> {
    const url: string = `${this.baseUrl}/`;
    return this.http.post<Team>(url, Team, this.httpOptions)
      .pipe(
        tap((newTeam: Team) => this.log(`adding team ${newTeam}`)),
        catchError(this.handleError<Team>(`adding ${Team}`))
      );
  }

  update(Team: Team): Observable<Team> {
    const url: string = `${this.baseUrl}/`;
    return this.http.put<Team>(url, Team, this.httpOptions)
      .pipe(
        tap((updatedTeam: Team) => this.log(`updating team ${updatedTeam}`)),
        catchError(this.handleError<Team>(`updating ${Team}`))
      );
  }

  private log(message: string) {
    this.loggerService.add(`TeamService: ${message}`);
  }

  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {

      // TODO: send the error to remote logging infrastructure
      console.error(error); // log to console instead

      // TODO: better job of transforming error for participant consumption
      this.log(`${operation} failed: ${error.message}`);

      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }

}
