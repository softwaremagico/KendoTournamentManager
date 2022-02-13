import {Injectable} from '@angular/core';
import {MatSnackBar} from '@angular/material/snack-bar';
import {TranslateService} from '@ngx-translate/core';
import {Observable, of} from "rxjs";
import {LoggerService} from "../logger.service";

@Injectable({
  providedIn: 'root'
})
export class MessageService {

  constructor(public snackBar: MatSnackBar, private translateService: TranslateService,
              private loggerService: LoggerService,) {
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
    this.openSnackBar(message, 'info-snackbar', 2000, undefined);
  }

  warningMessage(message: string) {
    this.openSnackBar(message, 'warning-snackbar', 2000, undefined);
  }

  errorMessage(message: string) {
    this.openSnackBar(message, 'error-snackbar', 5000, undefined);
  }

  handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {

      // TODO: send the error to remote logging infrastructure
      console.error(error); // log to console instead

      // TODO: better job of transforming error for tournament consumption
      this.log(`${operation} failed: ${error.message}`);
      this.errorMessage(`Error connecting to the backend service. ${operation} failed: ${error.message}`);

      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }

  log(message: string) {
    this.loggerService.add(`TournamentService: ${message}`);
  }
}
