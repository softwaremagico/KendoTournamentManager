import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {EnvironmentService} from "../environment.service";
import {MessageService} from "./message.service";
import {LoggerService} from "./logger.service";
import {LoginService} from "./login.service";
import {SystemOverloadService} from "./notifications/system-overload.service";
import {Observable} from "rxjs";
import {catchError, tap} from "rxjs/operators";
import {QrCode} from "../models/qr-code.model";
import {ParticipantImage} from "../models/participant-image.model";
import {Tournament} from "../models/tournament";

@Injectable({
  providedIn: 'root'
})
export class QrService {

  private baseUrl: string = this.environmentService.getBackendUrl() + '/qr';

  constructor(private http: HttpClient, private environmentService: EnvironmentService, private messageService: MessageService,
              private loggerService: LoggerService, public loginService: LoginService,
              private systemOverloadService: SystemOverloadService) {
  }

  getGuestsQr(tournament: Tournament): Observable<QrCode> {
    const url: string = `${this.baseUrl}/guest/tournament/${tournament.id}`;
    return this.http.get<QrCode>(url)
      .pipe(
        tap({
          next: () => this.loggerService.info(`Getting qr code from tournament ${tournament.id}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<QrCode>(`Getting qr code from tournament ${tournament.id}`))
      );
  }
}
