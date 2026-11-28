import {ChangeDetectionStrategy, Component, EventEmitter, Input, OnInit, Optional, Output} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';
import {Tournament} from '../../models/tournament';
import {QrService} from '../../services/qr.service';
import {QrCode} from '../../models/qr-code.model';
import {RbacActivity} from '../../services/rbac/rbac.activity';
import {RbacService} from '../../services/rbac/rbac.service';
import {UserSessionService} from '../../services/user-session.service';

@Component({
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false,
  selector: 'tournament-qr-code',
  templateUrl: './tournament-qr-code.component.html',
  styleUrls: ['./tournament-qr-code.component.scss']
})
export class TournamentQrCodeComponent implements OnInit {

  @Input()
  tournament: Tournament;
  @Input()
  port: number;
  @Input()
  mode: 'fights' | 'registration' = 'fights';
  @Output() closed: EventEmitter<void> = new EventEmitter<void>();

  qrCode: string | undefined;
  link: string | undefined;
  copied: boolean = false;

  protected readonly RbacActivity = RbacActivity;

  constructor(private readonly qrService: QrService, private readonly userSessionService: UserSessionService, public rbacService: RbacService,
              @Optional() private readonly translocoService?: TranslocoService) {
  }

  ngOnInit(): void {
    if (this.tournament) {
      const nightMode: boolean = this.userSessionService.getNightMode();
      const source = this.mode === 'registration'
        ? this.qrService.getRegistrationQr(this.tournament, nightMode, this.port)
        : this.qrService.getGuestsQr(this.tournament, nightMode, this.port);
      source.subscribe((_qrCode: QrCode): void => this.applyQrCode(_qrCode));
    }
  }

  private applyQrCode(_qrCode: QrCode | null | undefined): void {
    if (_qrCode) {
      this.qrCode = _qrCode.base64;
      this.link = _qrCode.content;
    } else {
      this.qrCode = undefined;
      this.link = undefined;
    }
    this.copied = false;
  }

  closeDialog(): void {
    this.closed.emit();
  }

  copyLink(): void {
    if (!this.link || !navigator.clipboard) {
      return;
    }
    void navigator.clipboard.writeText(this.link).then(
      () => this.copied = true,
      () => this.copied = false
    );
  }

  private download(blob: Blob, type: string, fileName: string): void {
    if (!blob) {
      return;
    }
    const downloadBlob: Blob = new Blob([blob], {type});
    const downloadURL: string = window.URL.createObjectURL(downloadBlob);
    const anchor: HTMLAnchorElement = document.createElement('a');
    anchor.download = fileName;
    anchor.href = downloadURL;
    anchor.click();
  }

  downloadQrAsPdf(): void {
    if (this.tournament) {
      if (this.mode === 'registration') {
        this.qrService.getRegistrationQrAsPdf(this.tournament, this.port).subscribe((pdf: Blob): void => {
          this.download(pdf, 'application/pdf', `Tournament - ${this.tournament.name} - registration QR .pdf`);
        });
      } else {
        this.qrService.getGuestsQrAsPdf(this.tournament).subscribe((html: Blob): void => {
          if (html !== null) {
            const blob: Blob = new Blob([html], {type: 'application/pdf'});
            const downloadURL: string = window.URL.createObjectURL(blob);

            const anchor: HTMLAnchorElement = document.createElement("a");
            anchor.download = "Tournament - " + this.tournament.name + " - QR .pdf";
            anchor.href = downloadURL;
            anchor.click();
          }
        });
      }
    }
  }

  downloadQrAsPng(): void {
    if (this.tournament && this.mode === 'registration') {
      this.qrService.getRegistrationQrAsPng(this.tournament, this.userSessionService.getNightMode(), this.port)
        .subscribe((png: Blob): void => {
          this.download(png, 'image/png', `Tournament - ${this.tournament.name} - registration QR.png`);
        });
    }
  }

  renewRegistrationQr(): void {
    if (!this.tournament || this.mode !== 'registration' || !this.rbacService.isAllowed(RbacActivity.EDIT_TOURNAMENT)) {
      return;
    }
    const warning: string = this.translocoService
      ? this.translocoService.translate('registrationQrRenewWarning')
      : 'Renewing invalidates the current registration link. Continue?';
    if (!window.confirm(warning)) {
      return;
    }
    this.qrService.renewRegistrationQr(this.tournament, this.userSessionService.getNightMode(), this.port)
      .subscribe((_qrCode: QrCode): void => this.applyQrCode(_qrCode));
  }
}
