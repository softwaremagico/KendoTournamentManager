import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {EnvironmentService} from "../environment.service";
import {LoginService} from "./login.service";
import {Log} from "./models/log";
import {catchError} from "rxjs/operators";
import {Observable, of} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class LoggerService {

  private readonly baseUrl: string = this.environmentService.getBackendUrl() + '/logger';

  constructor(private readonly http: HttpClient, private readonly environmentService: EnvironmentService, public loginService: LoginService) {
  }

  info(message: string) {
    const log: Log = new Log();
    log.message = message;
    this.sendInfo(log)
  }

  sendInfo(log: Log) {
    return this.sendLog('info', log, 'sendInfo');
  }

  warning(message: string) {
    const log: Log = new Log();
    log.message = message;
    this.sendWarning(log)
  }

  sendWarning(log: Log) {
    return this.sendLog('warning', log, 'sendWarning');
  }

  error(message: string) {
    const log: Log = new Log();
    log.message = message;
    this.sendError(log)
  }

  sendError(log: Log) {
    return this.sendLog('error', log, 'sendError');
  }

  private sendLog(level: 'info' | 'warning' | 'error', log: Log, operation: string) {
    const url: string = `${this.baseUrl}/${level}`;
    return this.http.post(url, log).pipe(
      catchError(this.handleErrorConsole(operation))
    ).subscribe();
  }

  handleErrorConsole<T>(_operation = 'operation', result?: T) {
    return (_error: unknown): Observable<T> => {
      return of(result as T);
    };
  }

  handleError<T>(operation = 'operation', result?: T) {
    return (error: unknown): Observable<T> => {
      const message: string = error instanceof Error ? error.message : String(error);
      this.error(`${operation} failed: ${message}`);

      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }
}
