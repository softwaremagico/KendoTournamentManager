import {Injectable} from '@angular/core';
import {HttpClient, HttpErrorResponse, HttpHeaders, HttpParams} from "@angular/common/http";
import {EnvironmentService} from "../environment.service";
import {LoginService} from "./login.service";
import {Observable, throwError} from "rxjs";
import {Tournament} from "../models/tournament";
import {catchError, tap} from "rxjs/operators";
import {MessageService} from "./message.service";
import {LoggerService} from "./logger.service";
import {SystemOverloadService} from "./notifications/system-overload.service";
import {Participant} from "../models/participant";
import {RoleType} from "../models/role-type";

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
    this.systemOverloadService.isBusy.next(true);
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

  getAccreditations(tournamentId: number, newOnes: boolean | undefined, roles: RoleType[]): Observable<Blob> {
    this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}/` + tournamentId + '/accreditations';
    return this.http.get<Blob>(url, {
      responseType: 'blob' as 'json', observe: 'body', headers: new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + this.loginService.getJwtValue()
      }),
      params: new HttpParams({
        fromObject: {
          'roles': roles,
          'onlyNew': newOnes!
        }
      })
    }).pipe(
      tap({
        next: () => this.loggerService.info(`getting tournament accreditations`),
        error: () => this.systemOverloadService.isBusy.next(false),
        complete: () => this.systemOverloadService.isBusy.next(false),
      }),
      catchError(this.messageService.handleError<Blob>(`getting tournament accreditations`))
    );
  }

  getParticipantAccreditation(tournamentId: number, participant: Participant, roleType: RoleType | undefined): Observable<Blob> {
    this.systemOverloadService.isBusy.next(true);
    if (roleType === undefined) {
      roleType = RoleType.getRandom();
    }
    const url: string = `${this.baseUrl}/` + tournamentId + '/accreditations/' + roleType;
    return this.http.post<Blob>(url, participant, {
      responseType: 'blob' as 'json', observe: 'body', headers: new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + this.loginService.getJwtValue()
      })
    }).pipe(
      tap({
        next: () => this.loggerService.info(`getting participant accreditations`),
        error: () => this.systemOverloadService.isBusy.next(false),
        complete: () => this.systemOverloadService.isBusy.next(false),
      }),
      catchError(this.messageService.handleError<Blob>(`getting participant accreditations`))
    );
  }

  getDiplomas(tournamentId: number, newOnes: boolean | undefined, roles: RoleType[]): Observable<Blob> {
    this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}/` + tournamentId + '/diplomas';
    return this.http.get<Blob>(url, {
      responseType: 'blob' as 'json', observe: 'body', headers: new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + this.loginService.getJwtValue()
      }),
      params: new HttpParams({
        fromObject: {
          'roles': roles,
          'onlyNew': newOnes!
        }
      })
    }).pipe(
      tap({
        next: () => this.loggerService.info(`getting tournament diplomas`),
        error: () => this.systemOverloadService.isBusy.next(false),
        complete: () => this.systemOverloadService.isBusy.next(false),
      }),
      catchError(this.messageService.handleError<Blob>(`getting tournament diplomas`))
    );
  }

  getParticipantDiploma(tournamentId: number, participant: Participant): Observable<Blob> {
    this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}/` + tournamentId + '/diplomas';
    return this.http.post<Blob>(url, participant, {
      responseType: 'blob' as 'json', observe: 'body', headers: new HttpHeaders({
        'Content-Type': 'application/json',
        'Authorization': 'Bearer ' + this.loginService.getJwtValue()
      })
    }).pipe(
      tap({
        next: () => this.loggerService.info(`getting participant diplomas`),
        error: () => this.systemOverloadService.isBusy.next(false),
        complete: () => this.systemOverloadService.isBusy.next(false),
      }),
      catchError(this.messageService.handleError<Blob>(`getting participant diplomas`))
    );
  }

  errorHandler(error: HttpErrorResponse): Observable<never> {
    if (error.error instanceof ErrorEvent) {
      // A client-side or network error occurred. Handle it accordingly.
      console.error('An error occurred:', error.error.message);
    } else {
      console.log(error.status)
      // The backend returned an unsuccessful response code.
      if (error.status == 404) {
        this.messageService.warningMessage('noResults');
      } else {
        // The response body may contain clues as to what went wrong,
        console.error(
          `Backend returned code ${error.status}, ` +
          `body was: ${error.error}`);
      }
    }
    // return an observable with a user-facing error message
    return throwError(() => new Error(error.message));
  }
}
