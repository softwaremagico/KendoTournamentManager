import {ErrorHandler, Injectable} from "@angular/core";
import { HttpErrorResponse } from "@angular/common/http";


@Injectable()
export class LocalErrorHandler implements ErrorHandler {

  handleError(error: Error | HttpErrorResponse): void {
    //These errors are already handled by HttpErrorInterceptor. If ok is set, already shown to the user.
    if (error instanceof HttpErrorResponse && !error.ok) {
      console.error(`Error connecting to the backend service. ${error.url} failed: ${error.message}`);
      return;
    }
    console.error(error);
  }
}
