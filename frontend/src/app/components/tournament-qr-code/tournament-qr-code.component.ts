import {Component, Inject, OnInit, Optional} from '@angular/core';
import {Tournament} from "../../models/tournament";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {Action} from "../../action";
import {QrService} from "../../services/qr.service";
import {QrCode} from "../../models/qr-code.model";
import {RbacActivity} from "../../services/rbac/rbac.activity";
import {RbacService} from "../../services/rbac/rbac.service";
import {UserSessionService} from "../../services/user-session.service";

@Component({
  selector: 'app-tournament-qr-code',
  templateUrl: './tournament-qr-code.component.html',
  styleUrls: ['./tournament-qr-code.component.scss']
})
export class TournamentQrCodeComponent implements OnInit {

  tournament: Tournament;
  port: number;
  qrCode: string | undefined;
  link: string | undefined;

  protected readonly RbacActivity = RbacActivity;

  constructor(public dialogRef: MatDialogRef<TournamentQrCodeComponent>,
              @Optional() @Inject(MAT_DIALOG_DATA) public data: { tournament: Tournament, port: number },
              private qrService: QrService, private userSessionService: UserSessionService, public rbacService: RbacService) {
    this.tournament = data.tournament;
    this.port = data.port;
  }

  ngOnInit(): void {
    this.qrService.getGuestsQr(this.tournament, this.userSessionService.getNightMode(), this.port).subscribe((_qrCode: QrCode): void => {
      if (_qrCode) {
        this.qrCode = _qrCode.base64;
        this.link = _qrCode.link;
      } else {
        this.qrCode = undefined;
      }
    });
  }

  closeDialog(): void {
    this.dialogRef.close({data: undefined, action: Action.Cancel});
  }


  downloadQrAsPdf(): void {
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
