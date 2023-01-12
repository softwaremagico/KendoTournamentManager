import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {EnvironmentService} from "../environment.service";
import {LoginService} from "./login.service";
import {Observable} from "rxjs";
import {Tournament} from "../models/tournament";
import {catchError, tap} from "rxjs/operators";
import {MessageService} from "./message.service";
import {LoggerService} from "./logger.service";
import {SystemOverloadService} from "./notifications/system-overload.service";

@Injectable({
  providedIn: 'root'
})
export class TournamentService {

  private baseUrl = this.environmentService.getBackendUrl() + '/tournaments';

  constructor(private http: HttpClient, private environmentService: EnvironmentService,
              public loginService: LoginService, private messageService: MessageService,
              private systemOverloadService: SystemOverloadService,
              private loggerService: LoggerService) {
  }

  getAll(): Observable<Tournament[]> {
    const url: string = `${this.baseUrl}`;

    // Why is not set yet????
    this.loginService.httpOptions.headers.get('Authorization');

    return this.http.get<Tournament[]>(url, this.loginService.httpOptions)
      .pipe(
        tap({
          next: () => this.loggerService.info(`fetched all Tournaments`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Tournament[]>(`gets all`))
      );
  }

  get(id: number): Observable<Tournament> {
    const url: string = `${this.baseUrl}/${id}`;
    return this.http.get<Tournament>(url, this.loginService.httpOptions)
      .pipe(
        tap({
          next: () => this.loggerService.info(`fetched tournament id=${id}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Tournament>(`get id=${id}`))
      );
  }

  deleteById(id: number) {
    const url: string = `${this.baseUrl}/${id}`;
    this.http.delete(url, this.loginService.httpOptions)
      .pipe(
        tap({
          next: () => this.loggerService.info(`deleting tournament id=${id}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Tournament>(`delete id=${id}`))
      );
  }

  delete(tournament: Tournament): Observable<Tournament> {
    const url: string = `${this.baseUrl}/delete`;
    return this.http.post<Tournament>(url, tournament, this.loginService.httpOptions)
      .pipe(
        tap({
          next: () => this.loggerService.info(`deleting tournament ${tournament}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Tournament>(`delete ${tournament}`))
      );
  }

  add(tournament: Tournament): Observable<Tournament> {
    const url: string = `${this.baseUrl}`;
    return this.http.post<Tournament>(url, tournament, this.loginService.httpOptions)
      .pipe(
        tap({
          next: () => (newTournament: Tournament) => this.loggerService.info(`adding tournament ${newTournament}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Tournament>(`adding ${tournament}`))
      );
  }


  update(tournament: Tournament): Observable<Tournament> {
    const url: string = `${this.baseUrl}`;
    return this.http.put<Tournament>(url, tournament, this.loginService.httpOptions)
      .pipe(
        tap({
          next: () => (updatedTournament: Tournament) => this.loggerService.info(`updating tournament ${updatedTournament}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Tournament>(`updating ${tournament}`))
      );
  }

  getAccreditations(tournamentId: number): Observable<Blob> {
    const url: string = `${this.baseUrl}/` + tournamentId + '/accreditations';
    return this.http.get<Blob>(url, {
      responseType: 'blob' as 'json', observe: 'body', headers: new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + this.loginService.getJwtValue()
      })
    });
  }

  getDiplomas(tournamentId: number): Observable<Blob> {
    const url: string = `${this.baseUrl}/` + tournamentId + '/diplomas';
    return this.http.get<Blob>(url, {
      responseType: 'blob' as 'json', observe: 'body', headers: new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + this.loginService.getJwtValue()
      })
    });
  }
}
