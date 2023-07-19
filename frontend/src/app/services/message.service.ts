import {Injectable} from '@angular/core';
import {MatSnackBar} from '@angular/material/snack-bar';
import {TranslateService} from '@ngx-translate/core';
import {Observable, of} from "rxjs";
import {LoggerService} from "./logger.service";
import {Log} from "./models/log";

@Injectable({
  providedIn: 'root'
})
export class MessageService {

  constructor(public snackBar: MatSnackBar, private translateService: TranslateService,
              private loggerService: LoggerService) {
  }

  private openSnackBar(message: string, cssClass: string, duration: number, action?: string) {
    this.snackBar.open(this.translateService.instant(message), action, {
      duration: duration,
      panelClass: [cssClass, 'message-service'],
      verticalPosition: 'top',
      horizontalPosition: 'right'
    });
  }


  infoMessage(message: string) {
    this.openSnackBar(message, 'info-snackbar', this.getDuration(message, 2));
  }

  warningMessage(message: string) {
    this.openSnackBar(message, 'warning-snackbar', this.getDuration(message, 3));
  }

  private getDuration(message: string, minDuration: number): number {
    return Math.max((message.length / 15), minDuration) * 1000;
  }

  errorMessage(message: string) {
    this.openSnackBar(message, 'error-snackbar', this.getDuration(message, 5));
  }

  handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      //Log error
      const log: Log = new Log();
      if (error) {
        log.message = `${operation} failed: ${error.message}`;
      }
      this.loggerService.sendError(log);

      //Show error
      this.errorMessage(`Error connecting to the backend service. ${operation} failed: ${error ? error.message : ""}`);

      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }

  logOnlyError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {
      //Log error
      const log: Log = new Log();
      log.message = `${operation} failed: ${error.message}`;
      this.loggerService.sendError(log);

      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }

}
