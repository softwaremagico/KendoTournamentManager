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

  constructor(private http: HttpClient, private environmentService: EnvironmentService, private messageService: MessageService,
              private loggerService: LoggerService, public authenticatedUserService: AuthenticatedUserService) {

  }

  getAll(): Observable<Fight[]> {
    const url: string = `${this.baseUrl}`;
    return this.http.get<Fight[]>(url, this.authenticatedUserService.httpOptions)
      .pipe(
        tap(_ => this.loggerService.info(`fetched all fights`)),
        catchError(this.messageService.handleError<Fight[]>(`gets all`))
      );
  }

  getFromTournament(tournament: Tournament): Observable<Fight[]> {
    const url: string = `${this.baseUrl}/tournaments/${tournament.id}`;
    return this.http.get<Fight[]>(url, this.authenticatedUserService.httpOptions)
      .pipe(
        tap(_ => this.loggerService.info(`fetched fights from tournament ${tournament.name}`)),
        catchError(this.messageService.handleError<Fight[]>(`get from tournament ${tournament}`))
      );
  }

  deleteById(id: number): Observable<number> {
    const url: string = `${this.baseUrl}/${id}`;
    return this.http.delete<number>(url, this.authenticatedUserService.httpOptions)
      .pipe(
        tap(_ => this.loggerService.info(`deleting fight id=${id}`)),
        catchError(this.messageService.handleError<number>(`delete id=${id}`))
      );
  }

  delete(fight: Fight): Observable<Fight> {
    const url: string = `${this.baseUrl}/delete`;
    return this.http.post<Fight>(url, fight, this.authenticatedUserService.httpOptions)
      .pipe(
        tap(_ => this.loggerService.info(`deleting fight ${fight}`)),
        catchError(this.messageService.handleError<Fight>(`delete ${fight}`))
      );
  }

  deleteCollection(fights: Fight[]): Observable<Fight[]> {
    const url: string = `${this.baseUrl}/delete/list`;
    return this.http.post<Fight[]>(url, fights, this.authenticatedUserService.httpOptions)
      .pipe(
        tap(_ => this.loggerService.info(`deleting fights ${fights}`)),
        catchError(this.messageService.handleError<Fight[]>(`delete ${fights}`))
      );
  }

  deleteByTournament(tournament: Tournament): Observable<Fight> {
    const url: string = `${this.baseUrl}/delete/tournaments`;
    return this.http.post<Fight>(url, {tournament: tournament}, this.authenticatedUserService.httpOptions)
      .pipe(
        tap(_ => this.loggerService.info(`deleting fights on ${tournament}`)),
        catchError(this.messageService.handleError<Fight>(`delete fights on ${tournament}`))
      );
  }

  add(fight: Fight): Observable<Fight> {
    const url: string = `${this.baseUrl}`;
    return this.http.post<Fight>(url, fight, this.authenticatedUserService.httpOptions)
      .pipe(
        tap((_newFight: Fight) => this.loggerService.info(`adding fight`)),
        catchError(this.messageService.handleError<Fight>(`adding fight`))
      );
  }

  addCollection(fights: Fight[]): Observable<Fight[]> {
    const url: string = `${this.baseUrl}` + '/list';
    return this.http.post<Fight[]>(url, fights, this.authenticatedUserService.httpOptions)
      .pipe(
        tap((_newFight: Fight[]) => this.loggerService.info(`adding fight`)),
        catchError(this.messageService.handleError<Fight[]>(`adding fight`))
      );
  }

  update(fight: Fight): Observable<Fight> {
    const url: string = `${this.baseUrl}`;
    return this.http.put<Fight>(url, fight, this.authenticatedUserService.httpOptions)
      .pipe(
        tap((_updatedFight: Fight) => this.loggerService.info(`updating fight`)),
        catchError(this.messageService.handleError<Fight>(`updating fight`))
      );
  }

  create(tournamentId: number, level: number, maximizeFights: boolean): Observable<Fight[]> {
    const url: string = `${this.baseUrl}` + '/create/tournaments/' + tournamentId + '/levels/' + level + '/maximize/' + maximizeFights;
    return this.http.put<Fight[]>(url, undefined, this.authenticatedUserService.httpOptions)
      .pipe(
        tap((_newFight: Fight[]) => this.loggerService.info(`adding fight`)),
        catchError(this.messageService.handleError<Fight[]>(`adding fight`))
      );
  }

  getFightSummaryPDf(tournamentId: number): Observable<Blob> {
    const url: string = `${this.baseUrl}` + '/tournaments/' + tournamentId + '/pdf';
    return this.http.get<Blob>(url, this.authenticatedUserService.httpOptions);
  }
}
