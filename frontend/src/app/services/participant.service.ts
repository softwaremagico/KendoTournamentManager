import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {EnvironmentService} from "../environment.service";
import {catchError, map, tap} from 'rxjs/operators';
import {Observable, of} from "rxjs";
import {Participant} from "../models/participant";
import {LoggerService} from "../logger.service";
import {Club} from "../models/club";
import {AuthenticatedUserService} from "./authenticated-user.service";

@Injectable({
  providedIn: 'root'
})
export class ParticipantService {

  private baseUrl = this.environmentService.getBackendUrl() + '/participants';

  httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + this.authenticatedUserService.getJwtValue()
    })
  };

  constructor(private http: HttpClient, private environmentService: EnvironmentService, private loggerService: LoggerService,
              public authenticatedUserService: AuthenticatedUserService) {
  }

  getAll(): Observable<Participant[]> {
    const url: string = `${this.baseUrl}/`;
    return this.http.get<Participant[]>(url, this.httpOptions)
      .pipe(
        tap(_ => this.log(`fetched all Participants`)),
        catchError(this.handleError<Participant[]>(`gets all`))
      );
  }

  get(id: number): Observable<Participant> {
    const url: string = `${this.baseUrl}/${id}`;
    return this.http.get<Participant>(url, this.httpOptions)
      .pipe(
        tap(_ => this.log(`fetched participant id=${id}`)),
        catchError(this.handleError<Participant>(`get id=${id}`))
      );
  }

  deleteById(id: number) {
    const url: string = `${this.baseUrl}/${id}`;
    this.http.delete(url, this.httpOptions)
      .pipe(
        tap(_ => this.log(`deleting participant id=${id}`)),
        catchError(this.handleError<Participant>(`delete id=${id}`))
      );
  }

  delete(participant: Participant): Observable<Participant> {
    const url: string = `${this.baseUrl}/delete`;
    return this.http.post<Participant>(url, participant, this.httpOptions)
      .pipe(
        tap(_ => this.log(`deleting participant ${participant}`)),
        catchError(this.handleError<Participant>(`delete ${participant}`))
      );
  }

  add(participant: Participant): Observable<Participant> {
    const url: string = `${this.baseUrl}`;
    return this.http.post<Participant>(url, participant, this.httpOptions)
      .pipe(
        tap((newParticipant: Participant) => this.log(`adding participant ${newParticipant}`)),
        catchError(this.handleError<Participant>(`adding ${participant}`))
      );
  }


  update(participant: Participant): Observable<Participant> {
    const url: string = `${this.baseUrl}`;
    return this.http.put<Participant>(url, participant, this.httpOptions)
      .pipe(
        tap((updatedParticipant: Participant) => this.log(`updating participant ${updatedParticipant}`)),
        catchError(this.handleError<Participant>(`updating ${participant}`))
      );
  }

  private log(message: string) {
    this.loggerService.add(`ParticipantService: ${message}`);
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
