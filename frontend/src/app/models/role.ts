import {Tournament} from "./tournament";
import {Participant} from "./participant";
import {RoleType} from "./role-type";
import {Element} from "./element";

export class Role extends Element {
  public tournament: Tournament;
  public participant: Participant;
  public roleType: RoleType;
  //A locked role cannot be deleted as is used on teams.
  public locked: boolean = true;


  public static override copy(source: Role, target: Role): void {
    Element.copy(source, target);
    target.roleType = source.roleType;
    if (source.tournament !== undefined) {
      target.tournament = Tournament.clone(source.tournament);
    }
    if (source.participant !== undefined) {
      target.participant = Participant.clone(source.participant);
    }
  }

  public static clone(data: Role): Role {
    const instance: Role = new Role();
    this.copy(data, instance);
    return instance;
  }
}
