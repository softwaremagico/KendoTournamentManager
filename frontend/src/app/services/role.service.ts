import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {EnvironmentService} from "../environment.service";
import {LoginService} from "./login.service";
import {Observable} from "rxjs";

import {catchError, tap} from "rxjs/operators";
import {Role} from "../models/role";
import {RoleType} from "../models/role-type";
import {Participant} from "../models/participant";
import {Tournament} from "../models/tournament";
import {MessageService} from "./message.service";
import {LoggerService} from "./logger.service";
import {SystemOverloadService} from "./notifications/system-overload.service";

@Injectable({
  providedIn: 'root'
})
export class RoleService {

  private baseUrl: string = this.environmentService.getBackendUrl() + '/roles';

  constructor(private http: HttpClient, private environmentService: EnvironmentService, private messageService: MessageService,
              private loggerService: LoggerService, private systemOverloadService: SystemOverloadService, public loginService: LoginService) {
  }

  getAll(): Observable<Role[]> {
    const url: string = `${this.baseUrl}`;
    return this.http.get<Role[]>(url)
      .pipe(
        tap({
          next: () => this.loggerService.info(`fetched all roles`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Role[]>(`gets all`))
      );
  }

  get(id: number): Observable<Role> {
    const url: string = `${this.baseUrl}/${id}`;
    return this.http.get<Role>(url)
      .pipe(
        tap({
          next: () => this.loggerService.info(`fetched role id=${id}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Role>(`get id=${id}`))
      );
  }

  getFromTournament(id: number): Observable<Role[]> {
    const url: string = `${this.baseUrl}/tournaments/${id}`;
    return this.http.get<Role[]>(url)
      .pipe(
        tap({
          next: () => this.loggerService.info(`fetched roles from tournament id=${id}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Role[]>(`get from tournament id=${id}`))
      );
  }

  getFromTournamentAndType(id: number, type: RoleType): Observable<Role[]> {
    const url: string = `${this.baseUrl}/tournaments/${id}/types/` + type;
    return this.http.get<Role[]>(url)
      .pipe(
        tap({
          next: () => this.loggerService.info(`fetched roles from tournament id=${id}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Role[]>(`get from tournament id=${id}`))
      );
  }

  getFromTournamentAndTypes(id: number, types: RoleType[]): Observable<Role[]> {
    const url: string = `${this.baseUrl}/tournaments/${id}/types/` + types.join(',');
    return this.http.get<Role[]>(url)
      .pipe(
        tap({
          next: () => this.loggerService.info(`fetched roles from tournament id=${id}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Role[]>(`get from tournament id=${id}`))
      );
  }

  deleteById(id: number): Observable<number> {
    const url: string = `${this.baseUrl}/${id}`;
    return this.http.delete<number>(url)
      .pipe(
        tap({
          next: () => this.loggerService.info(`deleting role id=${id}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<number>(`delete id=${id}`))
      );
  }

  delete(role: Role): Observable<Role> {
    const url: string = `${this.baseUrl}/delete`;
    return this.http.post<Role>(url, role)
      .pipe(
        tap({
          next: () => this.loggerService.info(`deleting role ${role}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Role>(`delete ${role}`))
      );
  }

  deleteByParticipantAndTournament(participant: Participant, tournament: Tournament): Observable<Role> {
    const url: string = `${this.baseUrl}/delete/participants`;
    return this.http.post<Role>(url, {
      participant: participant,
      tournament: tournament
    })
      .pipe(
        tap({
          next: () => this.loggerService.info(`deleting role for ${participant} on ${tournament}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Role>(`delete role for ${participant} on ${tournament}`))
      );
  }

  add(role: Role): Observable<Role> {
    const url: string = `${this.baseUrl}`;
    return this.http.post<Role>(url, role)
      .pipe(
        tap({
          next: (newRole: Role) => this.loggerService.info(`adding role ${newRole}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Role>(`adding ${role}`))
      );
  }

  update(role: Role): Observable<Role> {
    const url: string = `${this.baseUrl}`;
    return this.http.put<Role>(url, role)
      .pipe(
        tap({
          next: (updatedRole: Role) => this.loggerService.info(`updating role ${updatedRole}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Role>(`updating ${role}`))
      );
  }

  getRolesByTournament(tournamentId: number): Observable<Blob> {
    const url: string = `${this.baseUrl}` + '/tournaments/' + tournamentId + '/pdf';
    return this.http.get<Blob>(url, {
      responseType: 'blob' as 'json', observe: 'body', headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    });
  }
}
