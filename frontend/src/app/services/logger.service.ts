import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {EnvironmentService} from "../environment.service";
import {LoginService} from "./login.service";
import {Log} from "./models/log";
import {catchError} from "rxjs/operators";
import {Observable, of} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class LoggerService {

  private baseUrl: string = this.environmentService.getBackendUrl() + '/logger';

  constructor(private http: HttpClient, private environmentService: EnvironmentService, public loginService: LoginService) {
  }

  info(message: string) {
    const log: Log = new Log();
    log.message = message;
    this.sendInfo(log)
  }

  sendInfo(log: Log) {
    const url: string = `${this.baseUrl}/info`;
    return this.http.post(url, log).pipe(
      catchError(this.handleErrorConsole('sendInfo'))
    ).subscribe();
  }

  warning(message: string) {
    const log: Log = new Log();
    log.message = message;
    this.sendWarning(log)
  }

  sendWarning(log: Log) {
    const url: string = `${this.baseUrl}/warning`;
    return this.http.post(url, log).pipe(
      catchError(this.handleErrorConsole('sendWarning'))
    ).subscribe();
  }

  error(message: string) {
    const log: Log = new Log();
    log.message = message;
    this.sendError(log)
  }

  sendError(log: Log) {
    const url: string = `${this.baseUrl}/error`;
    return this.http.post(url, log).pipe(
      catchError(this.handleErrorConsole('sendError'))
    ).subscribe();
  }

  handleErrorConsole<T>(_operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      return of(result as T);
    };
  }

  handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      this.error(`${operation} failed: ${error.message}`);

      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }
}
