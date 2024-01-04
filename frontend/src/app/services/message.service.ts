import {Injectable, OnDestroy, OnInit} from '@angular/core';
import {MatSnackBar} from '@angular/material/snack-bar';
import {TranslateService} from '@ngx-translate/core';
import {Observable, of, Subscription} from "rxjs";
import {LoggerService} from "./logger.service";
import {Log} from "./models/log";
import {Message} from "@stomp/stompjs/esm6";
import {RxStompService} from "../websockets/rx-stomp.service";

@Injectable({
  providedIn: 'root'
})
export class MessageService implements OnInit, OnDestroy {

  private topicSubscription: Subscription;

  constructor(public snackBar: MatSnackBar, private translateService: TranslateService,
              private loggerService: LoggerService, private rxStompService: RxStompService) {
  }

  ngOnInit(): void {
    console.log('***REMOVED******REMOVED******REMOVED******REMOVED******REMOVED******REMOVED***-')
    this.topicSubscription = this.rxStompService.watch('/frontend/messages').subscribe((message: Message): void => {
      console.log(message.body);
    });
    this.rxStompService.publish({ destination: '/websockets/echo', body: 'Testing....' });
  }


  ngOnDestroy(): void {
    this.topicSubscription.unsubscribe();
  }


  private openSnackBar(message: string, cssClass: string, duration: number, action?: string): void {
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

  logOnlyError<T>(operation: string = 'operation', result?: T) {
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
