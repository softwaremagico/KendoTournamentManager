import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {EnvironmentService} from "../environment.service";
import {MessageService} from "./message.service";
import {LoggerService} from "./logger.service";
import {LoginService} from "./login.service";
import {SystemOverloadService} from "./notifications/system-overload.service";
import {Participant} from "../models/participant";
import {Observable} from "rxjs";
import {catchError, tap} from "rxjs/operators";
import {ParticipantImage} from "../models/participant-image.model";
import {Tournament} from "../models/tournament";
import {TournamentImageType} from "../models/tournament-image-type";
import {TournamentImage} from "../models/tournament-image.model";
import {ImageCompression} from "../models/image-compression";

@Injectable({
  providedIn: 'root'
})
export class FileService {

  private baseUrl: string = this.environmentService.getBackendUrl() + '/files';

  constructor(private http: HttpClient, private environmentService: EnvironmentService, private messageService: MessageService,
              private loggerService: LoggerService, public loginService: LoginService,
              private systemOverloadService: SystemOverloadService) {
  }

  setParticipantFilePicture(file: File, participant: Participant): Observable<ParticipantImage> {
    const url: string = `${this.baseUrl}/participants/${participant.id}`;
    const formData = new FormData();
    formData.append("file", file);
    formData.append("reportProgress", "true");
    return this.http.post<ParticipantImage>(url, formData)
      .pipe(
        tap({
          next: () => this.loggerService.info(`Adding picture to participant`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<ParticipantImage>(`adding file to participant ${participant.id}`))
      );
  }

  setTournamentFilePicture(file: File, tournament: Tournament, imageType: TournamentImageType, imageCompression: ImageCompression): Observable<TournamentImage> {
    this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}/tournaments/${tournament.id}/type/${imageType}/compression/${imageCompression}`;
    const formData = new FormData();
    formData.append("file", file);
    formData.append("reportProgress", "true");
    return this.http.post<TournamentImage>(url, formData)
      .pipe(
        tap({
          next: () => this.loggerService.info(`Adding file ${imageType} to tournament ${tournament.id}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<TournamentImage>(`adding file ${imageType} to tournament ${tournament.id}`))
      );
  }

  setBase64Picture(image: ParticipantImage): Observable<ParticipantImage> {
    this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}/participants`;
    return this.http.post<ParticipantImage>(url, image)
      .pipe(
        tap({
          next: () => this.loggerService.info(`Adding picture to participant`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<ParticipantImage>(`adding picture to ${image}`))
      );
  }

  getParticipantPicture(participant: Participant): Observable<ParticipantImage> {
    this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}/participants/${participant.id}`;
    return this.http.get<ParticipantImage>(url)
      .pipe(
        tap({
          next: () => this.loggerService.info(`Getting picture from participant`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<ParticipantImage>(`getting picture from ${participant}`))
      );
  }

  getTournamentPicture(tournament: Tournament, imageType: TournamentImageType): Observable<TournamentImage> {
    this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}/tournaments/${tournament.id}/type/${imageType}`;
    return this.http.get<TournamentImage>(url)
      .pipe(
        tap({
          next: () => this.loggerService.info(`Getting picture ${imageType} from tournament ${tournament.id}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<TournamentImage>(`getting picture ${imageType} from tournament ${tournament.id}`))
      );
  }

  deleteParticipantPicture(participant: Participant): Observable<void> {
    this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}/participants/${participant.id}`;
    return this.http.delete<void>(url)
      .pipe(
        tap({
          next: () => this.loggerService.info(`Deleting picture from participant`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<void>(`Deleting picture from ${participant}`))
      );
  }

  deleteTournamentPicture(tournament: Tournament, imageType: TournamentImageType): Observable<void> {
    this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}/tournaments/${tournament.id}/type/${imageType}`;
    return this.http.delete<void>(url)
      .pipe(
        tap({
          next: () => this.loggerService.info(`Deleting picture ${imageType} from tournament ${tournament.id}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<void>(`Deleting picture ${imageType} from tournament ${tournament.id}`))
      );
  }
}
