import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {EnvironmentService} from "../environment.service";
import {MessageService} from "./message.service";
import {LoggerService} from "./logger.service";
import {LoginService} from "./login.service";
import {Observable, of} from "rxjs";
import {catchError, map, tap} from "rxjs/operators";
import {Fight} from "../models/fight";
import {Tournament} from "../models/tournament";
import {SystemOverloadService} from "./notifications/system-overload.service";

@Injectable({
  providedIn: 'root'
})
export class FightService {

  private baseUrl: string = this.environmentService.getBackendUrl() + '/fights';

  constructor(private http: HttpClient, private environmentService: EnvironmentService, private messageService: MessageService,
              private loggerService: LoggerService, public loginService: LoginService,
              private systemOverloadService: SystemOverloadService) {

  }

  getAll(): Observable<Fight[]> {
    const url: string = `${this.baseUrl}`;
    return this.http.get<Fight[]>(url)
      .pipe(
        tap({
          next: () => this.loggerService.info(`fetched all fights`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Fight[]>(`gets all`))
      );
  }

  getFromTournament(tournament: Tournament): Observable<Fight[]> {
    const url: string = `${this.baseUrl}/tournaments/${tournament.id}`;
    return this.http.get<Fight[]>(url)
      .pipe(
        tap({
          next: () => this.loggerService.info(`fetched fights from tournament ${tournament.name}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Fight[]>(`get from tournament ${tournament}`))
      );
  }

  deleteById(id: number): Observable<number> {
    const url: string = `${this.baseUrl}/${id}`;
    return this.http.delete<number>(url)
      .pipe(
        tap({
          next: () => this.loggerService.info(`deleting fight id=${id}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<number>(`delete id=${id}`))
      );
  }

  delete(fight: Fight): Observable<Fight> {
    const url: string = `${this.baseUrl}/delete`;
    return this.http.post<Fight>(url, fight)
      .pipe(
        tap({
          next: () => this.loggerService.info(`deleting fight ${fight}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Fight>(`delete ${fight}`))
      );
  }

  deleteCollection(fights: Fight[]): Observable<Fight[]> {
    const url: string = `${this.baseUrl}/delete/list`;
    return this.http.post<Fight[]>(url, fights)
      .pipe(
        tap({
          next: () => this.loggerService.info(`deleting fights ${fights}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Fight[]>(`delete ${fights}`))
      );
  }

  deleteByTournament(tournament: Tournament): Observable<Fight> {
    const url: string = `${this.baseUrl}/delete/tournaments`;
    return this.http.post<Fight>(url, {tournament: tournament})
      .pipe(
        tap({
          next: () => this.loggerService.info(`deleting fights on ${tournament}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Fight>(`delete fights on ${tournament}`))
      );
  }

  add(fight: Fight): Observable<Fight> {
    const url: string = `${this.baseUrl}`;
    return this.http.post<Fight>(url, fight)
      .pipe(
        tap({
          next: (_newFight: Fight) => this.loggerService.info(`adding fight`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Fight>(`adding fight`))
      );
  }

  addCollection(fights: Fight[]): Observable<Fight[]> {
    const url: string = `${this.baseUrl}` + '/list';
    return this.http.post<Fight[]>(url, fights)
      .pipe(
        tap({
          next: (_newFight: Fight[]) => this.loggerService.info(`adding fight`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Fight[]>(`adding fight`))
      );
  }

  update(fight: Fight): Observable<Fight> {
    const url: string = `${this.baseUrl}`;
    return this.http.put<Fight>(url, fight)
      .pipe(
        tap({
          next: (_updatedFight: Fight) => this.loggerService.info(`updating fight`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Fight>(`updating fight`))
      );
  }

  updateAll(fights: Fight[]): Observable<Fight[]> {
    const url: string = `${this.baseUrl}/all`;
    return this.http.put<Fight[]>(url, fights)
      .pipe(
        tap({
          next: (_updatedFight: Fight[]) => this.loggerService.info(`updating fight`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Fight[]>(`updating fight`))
      );
  }

  create(tournamentId: number, level: number): Observable<Fight[]> {
    const url: string = `${this.baseUrl}` + '/create/tournaments/' + tournamentId + '/levels/' + level;
    return this.http.put<Fight[]>(url, undefined)
      .pipe(
        tap({
          next: (_newFight: Fight[]) => this.loggerService.info(`adding fight`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Fight[]>(`adding fight`))
      );
  }

  createNext(tournamentId: number): Observable<Fight[]> {
    const url: string = `${this.baseUrl}` + '/create/tournaments/' + tournamentId + '/next';
    return this.http.put<Fight[]>(url, undefined, {observe: 'response'})
      .pipe(
        map((response: any) => {
          //204 means that fights are not created due to an existing draw score.
          if (response.status === 204) {
            return of(null);
          }
          return response.body;
        })
      );
  }

  getFightSummaryPDf(tournamentId: number): Observable<Blob> {
    const url: string = `${this.baseUrl}` + '/tournaments/' + tournamentId + '/pdf';
    return this.http.get<Blob>(url, {
      responseType: 'blob' as 'json', observe: 'body', headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    }).pipe(
      tap({
        next: () => this.loggerService.info(`getting fights summary`),
        error: () => this.systemOverloadService.isBusy.next(false),
        complete: () => this.systemOverloadService.isBusy.next(false),
      }),
      catchError(this.messageService.handleError<Blob>(`getting fight summary`))
    );
  }

  generateDuels(fight: Fight): Observable<Fight> {
    const url: string = `${this.baseUrl}/duels`;
    return this.http.put<Fight>(url, fight)
      .pipe(
        tap({
          next: (_updatedFight: Fight) => this.loggerService.info(`generating duels for a fight`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Fight>(`generating duels for a fight`))
      );
  }
}
