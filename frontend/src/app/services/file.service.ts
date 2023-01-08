import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders, HttpParams} from "@angular/common/http";
import {EnvironmentService} from "../environment.service";
import {MessageService} from "./message.service";
import {LoggerService} from "./logger.service";
import {LoginService} from "./login.service";
import {SystemOverloadService} from "./notifications/system-overload.service";
import {Participant} from "../models/participant";
import {Observable} from "rxjs";
import {catchError, tap} from "rxjs/operators";
import {ParticipantImage} from "../models/participant-image.model";

@Injectable({
  providedIn: 'root'
})
export class FileService {

  private baseUrl = this.environmentService.getBackendUrl() + '/files';

  constructor(private http: HttpClient, private environmentService: EnvironmentService, private messageService: MessageService,
              private loggerService: LoggerService, public loginService: LoginService,
              private systemOverloadService: SystemOverloadService) {
  }

  setFilePicture(file: File, participant: Participant): Observable<ParticipantImage> {
    const url: string = `${this.baseUrl}/participants/${participant.id}`;
    const formData = new FormData();
    formData.append("file", file);
    formData.append("reportProgress", "true");
    const httpOptions = {
      headers: new HttpHeaders({
        'Authorization': 'Bearer ' + this.loginService.getJwtValue()
      })
    };
    return this.http.post<ParticipantImage>(url, formData, httpOptions)
      .pipe(
        tap({
          next: () => this.loggerService.info(`Adding picture to participant`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<ParticipantImage>(`adding file to participant ${participant.id}`))
      );
  }

  setBase64Picture(image: ParticipantImage): Observable<ParticipantImage> {
    const url: string = `${this.baseUrl}/participants`;
    return this.http.post<ParticipantImage>(url, image, this.loginService.httpOptions)
      .pipe(
        tap({
          next: () => this.loggerService.info(`Adding picture to participant`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<ParticipantImage>(`adding picture to ${image}`))
      );
  }

  getPicture(participant: Participant): Observable<ParticipantImage> {
    const url: string = `${this.baseUrl}/participants/${participant!.id}`;
    return this.http.get<ParticipantImage>(url, this.loginService.httpOptions)
      .pipe(
        tap({
          next: () => this.loggerService.info(`Getting picture from participant`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<ParticipantImage>(`getting picture from ${participant}`))
      );
  }
}
