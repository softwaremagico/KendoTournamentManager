import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {EnvironmentService} from "../environment.service";
import {MessageService} from "./message.service";
import {LoggerService} from "./logger.service";
import {AuthenticatedUserService} from "./authenticated-user.service";
import {Observable} from "rxjs";
import {catchError, tap} from "rxjs/operators";
import {Fight} from "../models/fight";
import {Tournament} from "../models/tournament";

@Injectable({
  providedIn: 'root'
})
export class FightService {

  private baseUrl = this.environmentService.getBackendUrl() + '/fights';

  httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + this.authenticatedUserService.getJwtValue()
    })
  };

  constructor(private http: HttpClient, private environmentService: EnvironmentService, private messageService: MessageService,
              private loggerService: LoggerService, public authenticatedUserService: AuthenticatedUserService) {

  }

  getAll(): Observable<Fight[]> {
    const url: string = `${this.baseUrl}/`;
    return this.http.get<Fight[]>(url, this.httpOptions)
      .pipe(
        tap(_ => this.loggerService.info(`fetched all fights`)),
        catchError(this.messageService.handleError<Fight[]>(`gets all`))
      );
  }

  getFromTournament(tournament: Tournament): Observable<Fight[]> {
    const url: string = `${this.baseUrl}/tournaments/${tournament.id}`;
    return this.http.get<Fight[]>(url, this.httpOptions)
      .pipe(
        tap(_ => this.loggerService.info(`fetched fights from tournament ${tournament.name}`)),
        catchError(this.messageService.handleError<Fight[]>(`get from tournament ${tournament}`))
      );
  }

  deleteById(id: number): Observable<number> {
    const url: string = `${this.baseUrl}/${id}`;
    return this.http.delete<number>(url, this.httpOptions)
      .pipe(
        tap(_ => this.loggerService.info(`deleting fight id=${id}`)),
        catchError(this.messageService.handleError<number>(`delete id=${id}`))
      );
  }

  delete(fight: Fight): Observable<Fight> {
    const url: string = `${this.baseUrl}/delete`;
    return this.http.post<Fight>(url, fight, this.httpOptions)
      .pipe(
        tap(_ => this.loggerService.info(`deleting fight ${fight}`)),
        catchError(this.messageService.handleError<Fight>(`delete ${fight}`))
      );
  }

  deleteByTournament(tournament: Tournament): Observable<Fight> {
    const url: string = `${this.baseUrl}/delete/tournaments`;
    return this.http.post<Fight>(url, {tournament: tournament}, this.httpOptions)
      .pipe(
        tap(_ => this.loggerService.info(`deleting fights on ${tournament}`)),
        catchError(this.messageService.handleError<Fight>(`delete fights on ${tournament}`))
      );
  }

  add(fight: Fight): Observable<Fight> {
    const url: string = `${this.baseUrl}/`;
    return this.http.post<Fight>(url, fight, this.httpOptions)
      .pipe(
        tap((newFight: Fight) => this.loggerService.info(`adding fight`)),
        catchError(this.messageService.handleError<Fight>(`adding fight`))
      );
  }

  update(fight: Fight): Observable<Fight> {
    const url: string = `${this.baseUrl}/`;
    return this.http.put<Fight>(url, fight, this.httpOptions)
      .pipe(
        tap((updatedFight: Fight) => this.loggerService.info(`updating fight`)),
        catchError(this.messageService.handleError<Fight>(`updating fight`))
      );
  }
}
