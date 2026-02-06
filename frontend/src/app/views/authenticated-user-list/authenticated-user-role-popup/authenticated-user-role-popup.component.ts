import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {AuthenticatedUser} from "../../../models/authenticated-user";
import {TRANSLOCO_SCOPE, TranslocoService} from "@ngneat/transloco";
import {UserRoles} from "../../../services/rbac/user-roles";
import {RbacService} from "../../../services/rbac/rbac.service";
import {RbacBasedComponent} from "../../../components/RbacBasedComponent";
import {UserService} from "../../../services/user.service";
import {MessageService} from "../../../services/message.service";

@Component({
  selector: 'authenticated-user-role-popup',
  templateUrl: './authenticated-user-role-popup.component.html',
  styleUrls: ['./authenticated-user-role-popup.component.scss'],
  providers: [
    {
      provide: TRANSLOCO_SCOPE,
      multi: true,
      useValue: {scope: '/', alias: 't'}
    }
  ]
})
export class AuthenticatedUserRolePopupComponent extends RbacBasedComponent implements OnInit {
  @Input() user: AuthenticatedUser | null;
  @Output() onClosed: EventEmitter<void> = new EventEmitter<void>();
  @Output() onSaved: EventEmitter<void> = new EventEmitter<void>();
  protected translatedRoles: { value: string, label: string, description: string }[] = [];
  selectedRole: string;

  constructor(rbacService: RbacService, private transloco: TranslocoService, private userService: UserService,
              private messageService: MessageService) {
    super(rbacService);
  }


  ngOnInit() {
    this.translateRoles();
    if (this.user) {
      this.selectedRole = this.user.roles[0];
    }
  }

  private translateRoles() {
    for (let role of UserRoles.toArray()) {
      this.translatedRoles.push({
        value: role,
        label: this.transloco.translate(role.toLowerCase()),
        description: this.transloco.translate(role.toLowerCase() + "Hint")
      })
    }
  }

  closeDialog(): void {
    this.onClosed.emit();
  }

  saveAction() {
    if (this.user) {
      const userRole: UserRoles | undefined = UserRoles.getByKey(this.selectedRole);
      if (userRole) {
        this.user.roles = [];
        this.user.roles.push(userRole);
        this.userService.update(this.user).subscribe((_user: AuthenticatedUser) => {
            this.messageService.infoMessage("roleChanged");
            this.onSaved.emit();
          }
        );
      }
    }
  }
}
