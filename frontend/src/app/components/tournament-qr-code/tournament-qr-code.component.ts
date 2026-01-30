import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Tournament} from "../../models/tournament";
import {QrService} from "../../services/qr.service";
import {QrCode} from "../../models/qr-code.model";
import {RbacActivity} from "../../services/rbac/rbac.activity";
import {RbacService} from "../../services/rbac/rbac.service";
import {UserSessionService} from "../../services/user-session.service";

@Component({
  selector: 'tournament-qr-code',
  templateUrl: './tournament-qr-code.component.html',
  styleUrls: ['./tournament-qr-code.component.scss']
})
export class TournamentQrCodeComponent implements OnInit {

  @Input()
  tournament: Tournament;
  @Input()
  port: number;
  @Output() onClosed: EventEmitter<void> = new EventEmitter<void>();

  qrCode: string | undefined;
  link: string | undefined;

  protected readonly RbacActivity = RbacActivity;

  constructor(private qrService: QrService, private userSessionService: UserSessionService, public rbacService: RbacService) {

  }

  ngOnInit(): void {
    if (this.tournament) {
      this.qrService.getGuestsQr(this.tournament, this.userSessionService.getNightMode(), this.port).subscribe((_qrCode: QrCode): void => {
        if (_qrCode) {
          this.qrCode = _qrCode.base64;
          this.link = _qrCode.content;
        } else {
          this.qrCode = undefined;
        }
      });
    }
  }

  closeDialog(): void {
    this.onClosed.emit();
  }


  downloadQrAsPdf(): void {
    if (this.tournament) {
      this.qrService.getGuestsQrAsPdf(this.tournament).subscribe((html: Blob): void => {
        if (html !== null) {
          const blob: Blob = new Blob([html], {type: 'application/pdf'});
          const downloadURL: string = window.URL.createObjectURL(blob);

          const anchor: HTMLAnchorElement = document.createElement("a");
          anchor.download = "Tournament - " + this.tournament + " - QR .pdf";
          anchor.href = downloadURL;
          anchor.click();
        }
      });
    }
  }
}
