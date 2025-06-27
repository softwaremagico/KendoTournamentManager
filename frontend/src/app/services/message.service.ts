import {Injectable, OnDestroy} from '@angular/core';
import {MatSnackBar} from '@angular/material/snack-bar';
import {TranslateService} from '@ngx-translate/core';
import {Observable, of, Subscription} from "rxjs";
import {LoggerService} from "./logger.service";
import {Log} from "./models/log";
import {Message} from "@stomp/stompjs/esm6";
import {RxStompService} from "../websockets/rx-stomp.service";
import {EnvironmentService} from "../environment.service";
import {MessageContent} from "../websockets/message-content.model";

@Injectable({
  providedIn: 'root'
})
export class MessageService implements OnDestroy {

  private websocketsPrefix: string = this.environmentService.getWebsocketPrefix();

  private messageSubscription: Subscription;

  constructor(public snackBar: MatSnackBar, private translateService: TranslateService,
              private loggerService: LoggerService, private rxStompService: RxStompService,
              private environmentService: EnvironmentService) {
    this.registerWebsocketsMessages();
  }


  ngOnDestroy(): void {
    this.messageSubscription.unsubscribe();
  }

  private registerWebsocketsMessages(): void {
    this.messageSubscription = this.rxStompService.watch(this.websocketsPrefix + '/messages').subscribe((message: Message): void => {
      try {
        const messageContent: MessageContent = JSON.parse(message.body);
        this.translateService.get(messageContent.payload, messageContent.parameters).subscribe((res: string): void => {
          let type: string = messageContent.type.toLowerCase();
          if (!type) {
            type = "info";
          }
          switch (type) {
            case "error":
              this.errorMessage(res);
              break;
            case "warning":
              this.warningMessage(res);
              break;
            case "info":
            default:
              this.infoMessage(res);
          }
        });

      } catch (e) {
        console.log("Invalid message payload", message.body);
      }
    });
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

  backendErrorMessage(error: number, code: string) {
    this.openSnackBar(`Error '${error}' with code '${code}' received.`, 'error-snackbar', this.getDuration(code, 5));
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
