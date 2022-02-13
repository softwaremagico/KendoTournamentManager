import {Element} from "./element";
import {Participant} from "./participant";
import {Tournament} from "./tournament";

export class Team extends Element {
  public name: string;
  public members: Participant[];
  public tournament: Tournament;
  public group?: number;

  public static override copy(source: Team, target: Team): void {
    Element.copy(source, target);
    target.name = source.name;
    if (source.members !== undefined) {
      target.members = [];
      for (let member of target.members) {
        target.members.push(Participant.clone(member));
      }
    }
    if (source.tournament !== undefined) {
      target.tournament = Tournament.clone(source.tournament);
    }
  }

  public static clone(data: Team): Team {
    const instance: Team = new Team();
    this.copy(data, instance);
    return instance;
  }
}
