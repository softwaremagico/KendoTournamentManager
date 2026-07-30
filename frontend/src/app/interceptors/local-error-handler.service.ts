import {ErrorHandler, Injectable} from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";
import {MessageService} from "../services/message.service";
import {LoggerService} from "../services/logger.service";


@Injectable()
export class LocalErrorHandler implements ErrorHandler {

  constructor(private readonly messageService: MessageService, private readonly loggerService: LoggerService) {
  }

  handleError(error: Error | HttpErrorResponse): void {
    //These errors are already handled by HttpErrorInterceptor. If ok is set, already shown to the user.
    if (error instanceof HttpErrorResponse && !error.ok) {
      //Show error
      this.messageService.errorMessage(`Error connecting to the backend service. ${error.url} failed: ${error.message}`);
    }
  }
}
