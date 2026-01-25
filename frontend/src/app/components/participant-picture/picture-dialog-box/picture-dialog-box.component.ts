import {Component, EventEmitter, Input, Output} from '@angular/core';
import {RbacBasedComponent} from "../../RbacBasedComponent";
import {RbacService} from "../../../services/rbac/rbac.service";

@Component({
  selector: 'picture-dialog-box',
  templateUrl: './picture-dialog-box.component.html',
  styleUrls: ['./picture-dialog-box.component.scss']
})
export class PictureDialogBoxComponent extends RbacBasedComponent {

  @Output()
  onClosed: EventEmitter<void> = new EventEmitter<void>();

  @Input()
  participantPicture: string;

  constructor(rbacService: RbacService) {
    super(rbacService);
  }

  closeDialog() {
    this.onClosed.emit();
  }

}
