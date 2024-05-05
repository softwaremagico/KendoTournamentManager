import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {EnvironmentService} from "../environment.service";
import {catchError, tap} from 'rxjs/operators';
import {Observable} from "rxjs";
import {Club} from "../models/club";
import {LoginService} from "./login.service";
import {MessageService} from "./message.service";
import {LoggerService} from "./logger.service";
import {SystemOverloadService} from "./notifications/system-overload.service";


@Injectable({
  providedIn: 'root'
})
export class ClubService {

  private baseUrl: string = this.environmentService.getBackendUrl() + '/clubs';

  constructor(private http: HttpClient, private environmentService: EnvironmentService, private messageService: MessageService,
              private loggerService: LoggerService, public loginService: LoginService,
              private systemOverloadService: SystemOverloadService) {
  }

  getAll(): Observable<Club[]> {
    const url: string = `${this.baseUrl}`;
    return this.http.get<Club[]>(url)
      .pipe(
        tap({
          next: () => this.loggerService.info(`fetched all clubs`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Club[]>(`gets all`))
      );
  }

  get(id: number): Observable<Club> {
    const url: string = `${this.baseUrl}/${id}`;
    return this.http.get<Club>(url)
      .pipe(
        tap({
          next: () => this.loggerService.info(`fetched club id=${id}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Club>(`get id=${id}`))
      );
  }

  deleteById(id: number): Observable<number> {
    const url: string = `${this.baseUrl}/${id}`;
    return this.http.delete<number>(url)
      .pipe(
        tap({
          next: () => this.loggerService.info(`deleting club id=${id}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<number>(`delete id=${id}`))
      );
  }

  delete(club: Club): Observable<Club> {
    const url: string = `${this.baseUrl}/delete`;
    return this.http.post<Club>(url, club)
      .pipe(
        tap({
          next: () => this.loggerService.info(`deleting club ${club}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Club>(`delete ${club}`))
      );
  }

  add(club: Club): Observable<Club> {
    const url: string = `${this.baseUrl}`;
    return this.http.post<Club>(url, club)
      .pipe(
        tap({
          next: (newClub: Club) => this.loggerService.info(`adding club ${newClub}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Club>(`adding ${club}`))
      );
  }

  update(club: Club): Observable<Club> {
    const url: string = `${this.baseUrl}`;
    return this.http.put<Club>(url, club)
      .pipe(
        tap({
          next: (updatedClub: Club) => this.loggerService.info(`updating club ${updatedClub}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Club>(`updating ${club}`))
      );
  }
}
