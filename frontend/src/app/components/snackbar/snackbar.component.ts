import {ChangeDetectionStrategy, ChangeDetectorRef, Component, OnDestroy, OnInit} from '@angular/core';
import {BiitSnackbarService, Notification} from "@biit-solutions/wizardry-theme/info";
import {Subscription} from "rxjs";

/**
 * Replacement for the wizardry-theme snackbar.
 *
 * The theme component assigns the notifications received from its service
 * without notifying the change detector, so with the Angular v22 OnPush default
 * the list is never rendered. This component keeps the same markup (the E2E
 * suite relies on `#biit-notification.error` and `#biit-notification.regular`)
 * and refreshes itself explicitly on every notification update.
 */
@Component({
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false,
  selector: 'app-snackbar',
  templateUrl: './snackbar.component.html',
  styleUrls: ['./snackbar.component.scss']
})
export class SnackbarComponent implements OnInit, OnDestroy {
  protected notifications: Notification[] = [];
  private subscription?: Subscription;

  constructor(protected readonly snackbarService: BiitSnackbarService, private readonly cdr: ChangeDetectorRef) {
  }

  ngOnInit(): void {
    this.subscription = this.snackbarService.getNotifications$.subscribe((notifications: Notification[]): void => {
      this.notifications = [...notifications];
      this.cdr.detectChanges();
    });
  }

  ngOnDestroy(): void {
    this.subscription?.unsubscribe();
  }
}
