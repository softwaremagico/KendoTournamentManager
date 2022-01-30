import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {EnvironmentService} from "../environment.service";
import {LoggerService} from "../logger.service";
import {AuthenticatedUserService} from "./authenticated-user.service";
import {Observable, of} from "rxjs";
import {Tournament} from "../models/tournament";
import {catchError, tap} from "rxjs/operators";

@Injectable({
  providedIn: 'root'
})
export class TournamentService {

  private baseUrl = this.environmentService.getBackendUrl() + '/tournaments';

  httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + this.authenticatedUserService.getJwtValue()
    })
  };

  constructor(private http: HttpClient, private environmentService: EnvironmentService, private loggerService: LoggerService,
              public authenticatedUserService: AuthenticatedUserService) { }

  getAll(): Observable<Tournament[]> {
    const url: string = `${this.baseUrl}/`;
    return this.http.get<Tournament[]>(url, this.httpOptions)
      .pipe(
        tap(_ => this.log(`fetched all Tournaments`)),
        catchError(this.handleError<Tournament[]>(`gets all`))
      );
  }

  get(id: number): Observable<Tournament> {
    const url: string = `${this.baseUrl}/${id}`;
    return this.http.get<Tournament>(url, this.httpOptions)
      .pipe(
        tap(_ => this.log(`fetched tournament id=${id}`)),
        catchError(this.handleError<Tournament>(`get id=${id}`))
      );
  }

  deleteById(id: number) {
    const url: string = `${this.baseUrl}/${id}`;
    this.http.delete(url, this.httpOptions)
      .pipe(
        tap(_ => this.log(`deleting tournament id=${id}`)),
        catchError(this.handleError<Tournament>(`delete id=${id}`))
      );
  }

  delete(tournament: Tournament): Observable<Tournament> {
    const url: string = `${this.baseUrl}/delete`;
    return this.http.post<Tournament>(url, tournament, this.httpOptions)
      .pipe(
        tap(_ => this.log(`deleting tournament ${tournament}`)),
        catchError(this.handleError<Tournament>(`delete ${tournament}`))
      );
  }

  add(tournament: Tournament): Observable<Tournament> {
    const url: string = `${this.baseUrl}`;
    return this.http.post<Tournament>(url, tournament, this.httpOptions)
      .pipe(
        tap((newTournament: Tournament) => this.log(`adding tournament ${newTournament}`)),
        catchError(this.handleError<Tournament>(`adding ${tournament}`))
      );
  }


  update(tournament: Tournament): Observable<Tournament> {
    const url: string = `${this.baseUrl}`;
    return this.http.put<Tournament>(url, tournament, this.httpOptions)
      .pipe(
        tap((updatedTournament: Tournament) => this.log(`updating tournament ${updatedTournament}`)),
        catchError(this.handleError<Tournament>(`updating ${tournament}`))
      );
  }

  private log(message: string) {
    this.loggerService.add(`TournamentService: ${message}`);
  }

  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {

      // TODO: send the error to remote logging infrastructure
      console.error(error); // log to console instead

      // TODO: better job of transforming error for tournament consumption
      this.log(`${operation} failed: ${error.message}`);

      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }
}
