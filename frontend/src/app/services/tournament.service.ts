import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {EnvironmentService} from "../environment.service";
import {AuthenticatedUserService} from "./authenticated-user.service";
import {Observable} from "rxjs";
import {Tournament} from "../models/tournament";
import {catchError, tap} from "rxjs/operators";
import {MessageService} from "./message.service";
import {LoggerService} from "./logger.service";

@Injectable({
  providedIn: 'root'
})
export class TournamentService {

  private baseUrl = this.environmentService.getBackendUrl() + '/tournaments';

  constructor(private http: HttpClient, private environmentService: EnvironmentService,
              public authenticatedUserService: AuthenticatedUserService, private messageService: MessageService,
              private loggerService: LoggerService) {
  }

  getAll(): Observable<Tournament[]> {
    const url: string = `${this.baseUrl}`;

    // Why is not set yet????
    this.authenticatedUserService.httpOptions.headers.get('Authorization');

    return this.http.get<Tournament[]>(url,  this.authenticatedUserService.httpOptions)
      .pipe(
        tap(_ => this.loggerService.info(`fetched all Tournaments`)),
        catchError(this.messageService.handleError<Tournament[]>(`gets all`))
      );
  }

  get(id: number): Observable<Tournament> {
    const url: string = `${this.baseUrl}/${id}`;
    return this.http.get<Tournament>(url, this.authenticatedUserService.httpOptions)
      .pipe(
        tap(_ => this.loggerService.info(`fetched tournament id=${id}`)),
        catchError(this.messageService.handleError<Tournament>(`get id=${id}`))
      );
  }

  deleteById(id: number) {
    const url: string = `${this.baseUrl}/${id}`;
    this.http.delete(url, this.authenticatedUserService.httpOptions)
      .pipe(
        tap(_ => this.loggerService.info(`deleting tournament id=${id}`)),
        catchError(this.messageService.handleError<Tournament>(`delete id=${id}`))
      );
  }

  delete(tournament: Tournament): Observable<Tournament> {
    const url: string = `${this.baseUrl}/delete`;
    return this.http.post<Tournament>(url, tournament, this.authenticatedUserService.httpOptions)
      .pipe(
        tap(_ => this.loggerService.info(`deleting tournament ${tournament}`)),
        catchError(this.messageService.handleError<Tournament>(`delete ${tournament}`))
      );
  }

  add(tournament: Tournament): Observable<Tournament> {
    const url: string = `${this.baseUrl}`;
    return this.http.post<Tournament>(url, tournament, this.authenticatedUserService.httpOptions)
      .pipe(
        tap((newTournament: Tournament) => this.loggerService.info(`adding tournament ${newTournament}`)),
        catchError(this.messageService.handleError<Tournament>(`adding ${tournament}`))
      );
  }


  update(tournament: Tournament): Observable<Tournament> {
    const url: string = `${this.baseUrl}`;
    return this.http.put<Tournament>(url, tournament, this.authenticatedUserService.httpOptions)
      .pipe(
        tap((updatedTournament: Tournament) => this.loggerService.info(`updating tournament ${updatedTournament}`)),
        catchError(this.messageService.handleError<Tournament>(`updating ${tournament}`))
      );
  }
}
