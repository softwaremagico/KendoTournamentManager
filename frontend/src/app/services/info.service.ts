import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {catchError, tap} from "rxjs/operators";
import {Observable} from "rxjs";
import {MessageService} from "./message.service";
import {EnvironmentService} from "../environment.service";
import {LoggerService} from "./logger.service";

@Injectable({
  providedIn: 'root'
})
export class InfoService {

  private baseUrl: string = this.environmentService.getBackendUrl() + '/info';

  constructor(private http: HttpClient, private messageService: MessageService, private environmentService: EnvironmentService,
              private loggerService: LoggerService) {
  }

  getLatestVersion(): Observable<string> {
    const url: string = `${this.baseUrl}/latest-version`;
    return this.http.get(url, {responseType: 'text'});
  }
}
