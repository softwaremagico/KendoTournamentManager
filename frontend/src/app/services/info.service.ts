import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {Observable} from "rxjs";
import {MessageService} from "./message.service";
import {EnvironmentService} from "../environment.service";
import {LoggerService} from "./logger.service";

@Injectable({
  providedIn: 'root'
})
export class InfoService {

  private readonly baseUrl: string = this.environmentService.getBackendUrl() + '/info';

  constructor(private readonly http: HttpClient, private readonly messageService: MessageService, private readonly environmentService: EnvironmentService,
              private readonly loggerService: LoggerService) {
  }

  getLatestVersion(): Observable<string> {
    const url: string = `${this.baseUrl}/latest-version`;
    return this.http.get(url, {responseType: 'text'});
  }

  getAppConfig(): Observable<Record<string, boolean>> {
    const url: string = `${this.baseUrl}/app-config`;
    return this.http.get<Record<string, boolean>>(url);
  }
}
