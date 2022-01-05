import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {EnvironmentService} from "../environment.service";
import {catchError, tap} from 'rxjs/operators';
import {Observable, of} from "rxjs";
import {Club} from "../models/club";
import {LoggerService} from "../logger.service";
import {AuthenticatedUserService} from "./authenticated-user.service";


@Injectable({
  providedIn: 'root'
})
export class ClubService {

  private baseUrl = this.environmentService.getBackendUrl() + '/clubs';

  httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + this.authenticatedUserService.getJwtValue()
    })
  };

  constructor(private http: HttpClient, private environmentService: EnvironmentService, private loggerService: LoggerService,
              public authenticatedUserService: AuthenticatedUserService) {
  }

  getAll(): Observable<Club[]> {
    const url: string = `${this.baseUrl}/`;
    return this.http.get<Club[]>(url, this.httpOptions)
      .pipe(
        tap(_ => this.log(`fetched all clubs`)),
        catchError(this.handleError<Club[]>(`gets all`))
      );
  }

  get(id: number): Observable<Club> {
    const url: string = `${this.baseUrl}/${id}`;
    return this.http.get<Club>(url, this.httpOptions)
      .pipe(
        tap(_ => this.log(`fetched club id=${id}`)),
        catchError(this.handleError<Club>(`get id=${id}`))
      );
  }

  deleteById(id: number): Observable<number> {
    const url: string = `${this.baseUrl}/${id}`;
    return this.http.delete<number>(url, this.httpOptions)
      .pipe(
        tap(_ => this.log(`deleting club id=${id}`)),
        catchError(this.handleError<number>(`delete id=${id}`))
      );
  }

  delete(club: Club): Observable<Club> {
    const url: string = `${this.baseUrl}`;
    return this.http.delete<Club>(url, this.httpOptions)
      .pipe(
        tap(_ => this.log(`deleting club ${club}`)),
        catchError(this.handleError<Club>(`delete ${club}`))
      );
  }

  add(club: Club): Observable<Club> {
    const url: string = `${this.baseUrl}/`;
    return this.http.post<Club>(url, club, this.httpOptions)
      .pipe(
        tap((newClub: Club) => this.log(`adding club ${newClub}`)),
        catchError(this.handleError<Club>(`adding ${club}`))
      );
  }

  update(club: Club): Observable<Club> {
    const url: string = `${this.baseUrl}/`;
    return this.http.put<Club>(url, club, this.httpOptions)
      .pipe(
        tap((updatedClub: Club) => this.log(`updating club ${updatedClub}`)),
        catchError(this.handleError<Club>(`updating ${club}`))
      );
  }

  private log(message: string) {
    this.loggerService.add(`ClubService: ${message}`);
  }

  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {

      // TODO: send the error to remote logging infrastructure
      console.error(error); // log to console instead

      // TODO: better job of transforming error for user consumption
      this.log(`${operation} failed: ${error.message}`);

      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }
}
