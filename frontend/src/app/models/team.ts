import {Element} from "./element";
import {Participant} from "./participant";
import {Tournament} from "./tournament";

export class Team extends Element {
  public name: string;
  public members: (Participant | undefined)[];
  public tournament: Tournament;
  public editing: boolean = false;
  //A locked team cannot be deleted as has fights.
  public locked: boolean = true;

  constructor(name?: string) {
    super();
    if (name) {
      this.name = name;
    }
  }

  public static override copy(source: Team, target: Team): void {
    Element.copy(source, target);
    target.name = source.name;
    if (source.members !== undefined) {
      target.members = [];
      for (let member of target.members) {
        if (member) {
          target.members.push(Participant.clone(member));
        } else {
          target.members.push(undefined);
        }
      }
    }
    target.tournament = Tournament.clone(source.tournament);
  }

  public static clone(data: Team): Team {
    const instance: Team = new Team();
    this.copy(data, instance);
    return instance;
  }
}
