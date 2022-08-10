import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {EnvironmentService} from "../environment.service";
import {catchError, tap} from 'rxjs/operators';
import {Observable} from "rxjs";
import {Club} from "../models/club";
import {AuthenticatedUserService} from "./authenticated-user.service";
import {MessageService} from "./message.service";
import {LoggerService} from "./logger.service";


@Injectable({
  providedIn: 'root'
})
export class ClubService {

  private baseUrl = this.environmentService.getBackendUrl() + '/clubs';

  constructor(private http: HttpClient, private environmentService: EnvironmentService, private messageService: MessageService,
              private loggerService: LoggerService, public authenticatedUserService: AuthenticatedUserService) {
  }

  getAll(): Observable<Club[]> {
    const url: string = `${this.baseUrl}`;
    return this.http.get<Club[]>(url, this.authenticatedUserService.httpOptions)
      .pipe(
        tap(_ => this.loggerService.info(`fetched all clubs`)),
        catchError(this.messageService.handleError<Club[]>(`gets all`))
      );
  }

  get(id: number): Observable<Club> {
    const url: string = `${this.baseUrl}/${id}`;
    return this.http.get<Club>(url, this.authenticatedUserService.httpOptions)
      .pipe(
        tap(_ => this.loggerService.info(`fetched club id=${id}`)),
        catchError(this.messageService.handleError<Club>(`get id=${id}`))
      );
  }

  deleteById(id: number): Observable<number> {
    const url: string = `${this.baseUrl}/${id}`;
    return this.http.delete<number>(url, this.authenticatedUserService.httpOptions)
      .pipe(
        tap(_ => this.loggerService.info(`deleting club id=${id}`)),
        catchError(this.messageService.handleError<number>(`delete id=${id}`))
      );
  }

  delete(club: Club): Observable<Club> {
    const url: string = `${this.baseUrl}/delete`;
    return this.http.post<Club>(url, club, this.authenticatedUserService.httpOptions)
      .pipe(
        tap(_ => this.loggerService.info(`deleting club ${club}`)),
        catchError(this.messageService.handleError<Club>(`delete ${club}`))
      );
  }

  add(club: Club): Observable<Club> {
    const url: string = `${this.baseUrl}`;
    return this.http.post<Club>(url, club, this.authenticatedUserService.httpOptions)
      .pipe(
        tap((newClub: Club) => this.loggerService.info(`adding club ${newClub}`)),
        catchError(this.messageService.handleError<Club>(`adding ${club}`))
      );
  }

  update(club: Club): Observable<Club> {
    const url: string = `${this.baseUrl}`;
    return this.http.put<Club>(url, club, this.authenticatedUserService.httpOptions)
      .pipe(
        tap((updatedClub: Club) => this.loggerService.info(`updating club ${updatedClub}`)),
        catchError(this.messageService.handleError<Club>(`updating ${club}`))
      );
  }
}
