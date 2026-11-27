import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from 'rxjs';
import {EnvironmentService} from '../environment.service';
import {LoggerService} from './logger.service';
import {MessageService} from './message.service';
import {SystemOverloadService} from './notifications/system-overload.service';
import {ServiceRequestMonitor} from './service-request-monitor';
import {RegistrationConfiguration, RegistrationRequest, RegistrationResponse} from './models/registration.model';

/**
 * Client for the credential-free self-registration endpoints.
 *
 * The invitation is a short-lived JWT supplied by the QR code as a query
 * parameter. It is only sent back as a query parameter of these public calls;
 * no login state or user session is modified.
 */
@Injectable({
  providedIn: 'root'
})
export class RegistrationService {

  private readonly monitor = new ServiceRequestMonitor(this.loggerService, this.messageService, this.systemOverloadService);

  private readonly baseUrl: string = this.environmentService.getBackendUrl() + '/registration/public';

  constructor(private readonly http: HttpClient, private readonly environmentService: EnvironmentService,
              private readonly messageService: MessageService, private readonly loggerService: LoggerService,
              private readonly systemOverloadService: SystemOverloadService) {
  }

  configuration(invitation: string): Observable<RegistrationConfiguration> {
    const url: string = `${this.baseUrl}/configuration?invitation=${encodeURIComponent(invitation ?? '')}`;
    return this.http.get<RegistrationConfiguration>(url)
      .pipe(this.monitor.track<RegistrationConfiguration>(`getting registration configuration`));
  }

  register(invitation: string, request: RegistrationRequest): Observable<RegistrationResponse> {
    const url: string = `${this.baseUrl}/register?invitation=${encodeURIComponent(invitation ?? '')}`;
    return this.http.post<RegistrationResponse>(url, request)
      .pipe(this.monitor.track<RegistrationResponse>(`registering participant`));
  }
}
