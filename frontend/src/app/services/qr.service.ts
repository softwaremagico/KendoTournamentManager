import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {EnvironmentService} from '../environment.service';
import {MessageService} from './message.service';
import {LoggerService} from './logger.service';
import {LoginService} from './login.service';
import {SystemOverloadService} from './notifications/system-overload.service';
import {ServiceRequestMonitor} from './service-request-monitor';
import {Observable} from 'rxjs';
import {QrCode} from '../models/qr-code.model';
import {Tournament} from '../models/tournament';

@Injectable({
  providedIn: 'root'
})
export class QrService {

  private readonly monitor = new ServiceRequestMonitor(this.loggerService, this.messageService, this.systemOverloadService);

  private readonly baseUrl: string = this.environmentService.getBackendUrl() + '/qr';

  constructor(private readonly http: HttpClient, private readonly environmentService: EnvironmentService, private readonly messageService: MessageService,
              private readonly loggerService: LoggerService, public readonly loginService: LoginService,
              private readonly systemOverloadService: SystemOverloadService) {
  }

  private registrationUrl(tournamentId: number | undefined, suffix: string = '', nightMode?: boolean, port?: number): string {
    const parameters: string[] = [];
    if (port !== undefined && port !== null) {
      parameters.push(`port=${port}`);
    }
    if (nightMode !== undefined && nightMode !== null) {
      parameters.push(`nightMode=${nightMode}`);
    }
    const query: string = parameters.length > 0 ? `?${parameters.join('&')}` : '';
    return `${this.baseUrl}/registration/tournament/${tournamentId}${suffix}${query}`;
  }

  private download(url: string, responseType: 'blob' | 'arraybuffer', errorContext: string): Observable<Blob> {
    return this.http.get<Blob>(url, {
      responseType: responseType as 'json', observe: 'body', headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    }).pipe(this.monitor.track<Blob>(errorContext));
  }

  getGuestsQr(tournament: Tournament, nightMode: boolean, port?: number): Observable<QrCode> {
    let url: string = `${this.baseUrl}/guest/tournament/${tournament.id}`;
    if (port) {
      url = `${this.baseUrl}/guest/tournament/${tournament.id}/port/${port}?nightMode=${nightMode}`;
    }
    return this.http.get<QrCode>(url)
      .pipe(this.monitor.track<QrCode>(`getting qr code from tournament ${tournament.id}`));
  }

  getGuestsQrAsPdf(tournament: Tournament, port?: number): Observable<Blob> {
    let url: string = `${this.baseUrl}/guest/tournament/${tournament.id}/pdf`;
    if (port) {
      url = `${this.baseUrl}/guest/tournament/${tournament.id}/pdf/port/${port}`;
    }
    this.systemOverloadService.isBusy.next(true);
    return this.download(url, 'blob', `getting qr code as pdf from tournament ${tournament.id}`);
  }

  getRegistrationQr(tournament: Tournament, nightMode: boolean, port?: number): Observable<QrCode> {
    return this.http.get<QrCode>(this.registrationUrl(tournament.id, '', nightMode, port))
      .pipe(this.monitor.track<QrCode>(`getting registration qr code from tournament ${tournament.id}`));
  }

  getRegistrationQrAsPng(tournament: Tournament, nightMode: boolean, port?: number): Observable<Blob> {
    this.systemOverloadService.isBusy.next(true);
    return this.download(this.registrationUrl(tournament.id, '/png', nightMode, port), 'blob',
      `getting registration qr code as png from tournament ${tournament.id}`);
  }

  getRegistrationQrAsPdf(tournament: Tournament, port?: number): Observable<Blob> {
    this.systemOverloadService.isBusy.next(true);
    return this.download(this.registrationUrl(tournament.id, '/pdf', undefined, port), 'blob',
      `getting registration qr code as pdf from tournament ${tournament.id}`);
  }

  renewRegistrationQr(tournament: Tournament, nightMode: boolean, port?: number): Observable<QrCode> {
    return this.http.post<QrCode>(this.registrationUrl(tournament.id, '/renew', nightMode, port), null)
      .pipe(this.monitor.track<QrCode>(`renewing registration qr code from tournament ${tournament.id}`));
  }

  getParticipantQr(participantId: number, nightMode: boolean, port?: number): Observable<QrCode> {
    let url: string = `${this.baseUrl}/participant/${participantId}/statistics`;
    if (port) {
      url = `${this.baseUrl}/participant/${participantId}/statistics/port/${port}?nightMode=${nightMode}`;
    }
    return this.http.get<QrCode>(url)
      .pipe(this.monitor.track<QrCode>(`getting qr code from participant ${participantId}`));
  }
}
