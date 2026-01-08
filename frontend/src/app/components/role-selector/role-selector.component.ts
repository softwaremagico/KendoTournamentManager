import {Component, EventEmitter, Input, Output} from '@angular/core';
import {Tournament} from "../../models/tournament";
import {RoleType} from "../../models/role-type";

@Component({
  selector: 'role-selector',
  templateUrl: './role-selector.component.html',
  styleUrls: ['./role-selector.component.scss']
})
export class RoleSelectorComponent {

  @Input()
  tournament: Tournament;

  roles: RoleType[] = [];

  @Output()
  onClosed: EventEmitter<{ tournament: Tournament, roles: RoleType[], newOnes: boolean }> = new EventEmitter<{
    tournament: Tournament,
    roles: RoleType[],
    newOnes: boolean
  }>();

  roleTypes: RoleType[] = RoleType.toArray();

  constructor() {
  }

  setRoles(newOnes: boolean): void {
    this.onClosed.emit({tournament: this.tournament, roles: this.roles, newOnes: newOnes})
  }

  closeDialog(): void {
    this.onClosed.emit();
  }

  select(checked: boolean, roleType: RoleType): void {
    if (checked) {
      this.roles.push(roleType);
    } else {
      this.roles = this.roles.filter((item: RoleType): boolean => item !== roleType);
    }
  }
}
