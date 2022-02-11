import {Injectable} from '@angular/core';
import {MatSnackBar} from '@angular/material/snack-bar';
import {TranslateService} from '@ngx-translate/core';

@Injectable({
  providedIn: 'root'
})
export class MessageService {

  constructor(public snackBar: MatSnackBar, private translateService: TranslateService) {
  }

  private openSnackBar(message: string, cssClass: string, action?: string) {
    this.snackBar.open(this.translateService.instant(message), action, {
      duration: 2000,
      panelClass: [cssClass, 'message-service'],
      verticalPosition: 'top',
      horizontalPosition: 'right'
    });
  }


  infoMessage(message: string) {
    this.openSnackBar(message, 'info-snackbar', undefined);
  }

  warningMessage(message: string) {
    this.openSnackBar(message, 'warning-snackbar', undefined);
  }

  errorMessage(message: string) {
    this.openSnackBar(message, 'error-snackbar', undefined);
  }
}
