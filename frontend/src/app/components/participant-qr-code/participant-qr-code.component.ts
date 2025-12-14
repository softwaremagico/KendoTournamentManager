import {Component, EventEmitter, Inject, Input, OnInit, Output} from '@angular/core';
import {QrCode} from "../../models/qr-code.model";
import {QrService} from "../../services/qr.service";
import {RbacService} from "../../services/rbac/rbac.service";
import {RbacActivity} from "../../services/rbac/rbac.activity";
import {UserSessionService} from "../../services/user-session.service";
import {Participant} from "../../models/participant";

@Component({
  selector: 'participant-qr-code',
  templateUrl: './participant-qr-code.component.html',
  styleUrls: ['./participant-qr-code.component.scss']
})
export class ParticipantQrCodeComponent implements OnInit {

  @Input()
  participant: Participant;
  @Input()
  port: number;
  @Output() onClosed: EventEmitter<void> = new EventEmitter<void>();

  protected qrCode: string | undefined;
  protected link: string | undefined;

  protected readonly RbacActivity = RbacActivity;

  constructor(private qrService: QrService, private userSessionService: UserSessionService, public rbacService: RbacService) {

  }

  ngOnInit(): void {
    if (this.participant) {
      this.qrService.getParticipantQr(this.participant.id!, this.userSessionService.getNightMode(), this.port).subscribe((_qrCode: QrCode): void => {
        if (_qrCode) {
          this.qrCode = _qrCode.base64;
          this.link = _qrCode.link;
        } else {
          this.qrCode = undefined;
        }
      });
    }
  }

  close() {
    this.onClosed.emit();
  }
}
