import {ErrorHandler, Injectable} from "@angular/core";
import {HttpErrorResponse} from "@angular/common/http";
import {MessageService} from "../services/message.service";
import {LoggerService} from "../services/logger.service";


@Injectable()
export class LocalErrorHandler implements ErrorHandler {

  constructor(private messageService: MessageService, private loggerService: LoggerService) {
  }

  handleError(error: any): void {
    //Show on console!
    console.error(error);
    //These errors are already handled by HttpErrorInterceptor
    if (error instanceof HttpErrorResponse) {
      //Show error
      this.messageService.errorMessage(`Error connecting to the backend service. ${error.url} failed: ${error ? error.message : ""}`);
    }
  }
}
