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
import {LoginService} from './login.service';

@Injectable({
  providedIn: 'root'
})
export class MessageService implements OnDestroy {
  private static readonly MESSAGE_DEDUPLICATION_MILLIS = 10000;

  private websocketsPrefix: string = this.environmentService.getWebsocketPrefix();

  private messageSubscription: Subscription;
  private readonly recentMessages: Map<string, number> = new Map<string, number>();

  constructor(public readonly snackBar: BiitSnackbarService, private readonly translateService: TranslocoService,
              private readonly loggerService: LoggerService, private readonly rxStompService: RxStompService,
               private readonly environmentService: EnvironmentService, private readonly loginService: LoginService) {
    this.registerWebsocketsMessages();
  }


  ngOnDestroy(): void {
    this.messageSubscription.unsubscribe();
  }

  private registerWebsocketsMessages(): void {
    this.messageSubscription = this.rxStompService.watch(this.websocketsPrefix + '/tenant/' + this.loginService.getTenantId() + '/messages').subscribe((message: Message): void => {
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
    const translatedMessage = this.translateService.translate(message);
    if (this.wasRecentlyShown(`${type}:${translatedMessage}`)) {
      return;
    }
    this.snackBar.showNotification(translatedMessage, type, action, duration)
  }

  private wasRecentlyShown(message: string): boolean {
    const now = Date.now();
    const previous = this.recentMessages.get(message);
    this.recentMessages.set(message, now);
    for (const [recentMessage, timestamp] of this.recentMessages) {
      if (now - timestamp > MessageService.MESSAGE_DEDUPLICATION_MILLIS) {
        this.recentMessages.delete(recentMessage);
      }
    }
    return previous !== undefined && now - previous < MessageService.MESSAGE_DEDUPLICATION_MILLIS;
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
