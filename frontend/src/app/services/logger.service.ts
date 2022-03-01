import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {EnvironmentService} from "../environment.service";
import {AuthenticatedUserService} from "./authenticated-user.service";
import {Log} from "./models/log";
import {catchError} from "rxjs/operators";
import {Observable, of} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class LoggerService {

  private baseUrl = this.environmentService.getBackendUrl() + '/logger';

  httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + this.authenticatedUserService.getJwtValue()
    })
  };

  constructor(private http: HttpClient, private environmentService: EnvironmentService, public authenticatedUserService: AuthenticatedUserService) {
  }

  info(message: string) {
    const log: Log = new Log();
    log.message = message;
    this.sendInfo(log)
  }

  sendInfo(log: Log) {
    const url: string = `${this.baseUrl}/info`;

    console.log(log.message);
    return this.http.post(url, log, this.httpOptions).pipe(
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
    return this.http.post(url, log, this.httpOptions).pipe(
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
    return this.http.post(url, log, this.httpOptions).pipe(
      catchError(this.handleErrorConsole('sendError'))
    ).subscribe();
  }

  handleErrorConsole<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      console.error(error);
      return of(result as T);
    };
  }

  handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      console.error(error); // Also log to console.
      this.error(`${operation} failed: ${error.message}`);

      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }
}
