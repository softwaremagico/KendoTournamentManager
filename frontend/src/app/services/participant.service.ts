import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {EnvironmentService} from "../environment.service";
import {catchError, tap} from 'rxjs/operators';
import {Observable} from "rxjs";
import {Participant} from "../models/participant";
import {LoginService} from "./login.service";
import {MessageService} from "./message.service";
import {LoggerService} from "./logger.service";
import {SystemOverloadService} from "./notifications/system-overload.service";

@Injectable({
  providedIn: 'root'
})
export class ParticipantService {

  private baseUrl: string = this.environmentService.getBackendUrl() + '/participants';

  constructor(private http: HttpClient, private environmentService: EnvironmentService, private messageService: MessageService,
              private loggerService: LoggerService, public loginService: LoginService,
              private systemOverloadService: SystemOverloadService) {
  }

  getAll(): Observable<Participant[]> {
    const url: string = `${this.baseUrl}`;
    return this.http.get<Participant[]>(url)
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
    return this.http.get<Participant>(url)
      .pipe(
        tap({
          next: () => this.loggerService.info(`fetched participant id=${id}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<Participant>(`get id=${id}`))
      );
  }

  deleteById(id: number): void {
    const url: string = `${this.baseUrl}/${id}`;
    this.http.delete(url)
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
    return this.http.post<Participant>(url, participant)
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
    return this.http.post<Participant>(url, participant)
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
    return this.http.put<Participant>(url, participant)
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
