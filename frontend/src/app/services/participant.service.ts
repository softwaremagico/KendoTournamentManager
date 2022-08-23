import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {EnvironmentService} from "../environment.service";
import {catchError, tap} from 'rxjs/operators';
import {Observable} from "rxjs";
import {Participant} from "../models/participant";
import {AuthenticatedUserService} from "./authenticated-user.service";
import {MessageService} from "./message.service";
import {LoggerService} from "./logger.service";
import {SystemOverloadService} from "./system-overload.service";

@Injectable({
  providedIn: 'root'
})
export class ParticipantService {

  private baseUrl = this.environmentService.getBackendUrl() + '/participants';

  constructor(private http: HttpClient, private environmentService: EnvironmentService, private messageService: MessageService,
              private loggerService: LoggerService, public authenticatedUserService: AuthenticatedUserService,
              private systemOverloadService: SystemOverloadService) {
  }

  getAll(): Observable<Participant[]> {
    const url: string = `${this.baseUrl}`;
    return this.http.get<Participant[]>(url, this.authenticatedUserService.httpOptions)
      .pipe(
        tap({
          next: () => this.loggerService.info(`fetched all Participants`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Participant[]>(`gets all`))
      );
  }

  get(id: number): Observable<Participant> {
    const url: string = `${this.baseUrl}/${id}`;
    return this.http.get<Participant>(url, this.authenticatedUserService.httpOptions)
      .pipe(
        tap({
          next: () => this.loggerService.info(`fetched participant id=${id}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Participant>(`get id=${id}`))
      );
  }

  deleteById(id: number) {
    const url: string = `${this.baseUrl}/${id}`;
    this.http.delete(url, this.authenticatedUserService.httpOptions)
      .pipe(
        tap({
          next: () => this.loggerService.info(`deleting participant id=${id}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Participant>(`delete id=${id}`))
      );
  }

  delete(participant: Participant): Observable<Participant> {
    const url: string = `${this.baseUrl}/delete`;
    return this.http.post<Participant>(url, participant, this.authenticatedUserService.httpOptions)
      .pipe(
        tap({
          next: () => this.loggerService.info(`deleting participant ${participant}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Participant>(`delete ${participant}`))
      );
  }

  add(participant: Participant): Observable<Participant> {
    const url: string = `${this.baseUrl}`;
    return this.http.post<Participant>(url, participant, this.authenticatedUserService.httpOptions)
      .pipe(
        tap({
          next: (newParticipant: Participant) => this.loggerService.info(`adding participant ${newParticipant}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Participant>(`adding ${participant}`))
      );
  }


  update(participant: Participant): Observable<Participant> {
    const url: string = `${this.baseUrl}`;
    return this.http.put<Participant>(url, participant, this.authenticatedUserService.httpOptions)
      .pipe(
        tap({
          next: (updatedParticipant: Participant) => this.loggerService.info(`updating participant ${updatedParticipant}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Participant>(`updating ${participant}`))
      );
  }
}
