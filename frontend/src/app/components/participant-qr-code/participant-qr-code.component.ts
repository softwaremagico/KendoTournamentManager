import {Component, Inject, OnInit, Optional} from '@angular/core';
import {QrCode} from "../../models/qr-code.model";
import {Action} from "../../action";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {QrService} from "../../services/qr.service";
import {RbacService} from "../../services/rbac/rbac.service";
import {RbacActivity} from "../../services/rbac/rbac.activity";
import {UserSessionService} from "../../services/user-session.service";

@Component({
  selector: 'app-participant-qr-code',
  templateUrl: './participant-qr-code.component.html',
  styleUrls: ['./participant-qr-code.component.scss']
})
export class ParticipantQrCodeComponent implements OnInit {

  participantId: number;
  port: number;
  qrCode: string | undefined;
  link: string | undefined;

  constructor(public dialogRef: MatDialogRef<ParticipantQrCodeComponent>,
              @Optional() @Inject(MAT_DIALOG_DATA) public data: { participantId: number, port: number },
              private qrService: QrService, private userSessionService: UserSessionService, public rbacService: RbacService) {
    this.participantId = data.participantId;
    this.port = data.port;
  }

  ngOnInit(): void {
    this.qrService.getParticipantQr(this.participantId, this.userSessionService.getNightMode(), this.port).subscribe((_qrCode: QrCode): void => {
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

  protected readonly RbacActivity = RbacActivity;
}
