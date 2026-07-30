import {Injectable, OnDestroy} from '@angular/core';
import {Observable, of, Subscription} from "rxjs";
import {LoggerService} from "./logger.service";
import {Log} from "./models/log";
import {Message} from "@stomp/stompjs";
import {RxStompService} from "../websockets/rx-stomp.service";
import {EnvironmentService} from "../environment.service";
import {MessageContent} from "../websockets/message-content.model";
import {TranslocoService} from '@jsverse/transloco';
import {BiitSnackbarService, NotificationType} from "@biit-solutions/wizardry-theme/info";

@Injectable({
  providedIn: 'root'
})
export class MessageService implements OnDestroy {

  private websocketsPrefix: string = this.environmentService.getWebsocketPrefix();

  private messageSubscription: Subscription;

  constructor(public readonly snackBar: BiitSnackbarService, private readonly translateService: TranslocoService,
              private readonly loggerService: LoggerService, private readonly rxStompService: RxStompService,
              private readonly environmentService: EnvironmentService) {
    this.registerWebsocketsMessages();
  }


  ngOnDestroy(): void {
    this.messageSubscription.unsubscribe();
  }

  private registerWebsocketsMessages(): void {
    this.messageSubscription = this.rxStompService.watch(this.websocketsPrefix + '/messages').subscribe((message: Message): void => {
      try {
        const messageContent: MessageContent = JSON.parse(message.body);
        const res: string = this.translateService.translate(messageContent.payload, messageContent.parameters);
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

      } catch (e) {
        console.error("Invalid message payload", message.body);
      }
    });
  }

  private openSnackBar(message: string, type: NotificationType, duration: number, action?: string): void {
    this.snackBar.showNotification(this.translateService.translate(message), type, action, duration)
  }


  infoMessage(message: string): void {
    this.openSnackBar(message, NotificationType.SUCCESS, this.getDuration(message, 4));
  }

  warningMessage(message: string): void {
    this.openSnackBar(message, NotificationType.WARNING, this.getDuration(message, 7));
  }

  private getDuration(message: string, minDuration: number): number {
    return Math.max((message.length / 15), minDuration);
  }

  errorMessage(message: string): void {
    this.openSnackBar(message, NotificationType.ERROR, this.getDuration(message, 10));
  }

  backendErrorMessage(error: number, code: string): void {
    this.openSnackBar(`Error '${error}' with code '${code}' received.`, NotificationType.ERROR, this.getDuration(code, 5));
  }

  handleError<T>(operation = 'operation', result?: T) {
    return (error: unknown): Observable<T> => {
      const errorMessage: string = error instanceof Error ? error.message : String(error ?? '');
      //Log error
      const log: Log = new Log();
      log.message = `${operation} failed: ${errorMessage}`;
      this.loggerService.sendError(log);

      //Show error
      this.errorMessage(`Error connecting to the backend service. ${operation} failed: ${errorMessage}`);

      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }

  logOnlyError<T>(operation: string = 'operation', result?: T) {
    return (error: unknown): Observable<T> => {
      const errorMessage: string = error instanceof Error ? error.message : String(error ?? '');
      //Log error
      const log: Log = new Log();
      log.message = `${operation} failed: ${errorMessage}`;
      this.loggerService.sendError(log);

      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }

}
