import {Element} from "./element";
import {Tournament} from "./tournament";
import {Team} from "./team";
import {Duel} from "./duel";

export class Fight extends Element {
  public team1: Team;
  public team2: Team;
  public tournament: Tournament;
  public shiaijo: number;
  public finishedAt: Date;
  public level: number;
  public duels: Duel[];


  public static override copy(source: Fight, target: Fight): void {
    Element.copy(source, target);
    if (source.tournament !== undefined) {
      target.tournament = Tournament.clone(source.tournament);
    }
    target.shiaijo = source.shiaijo;
    target.level = source.level;
    target.finishedAt = source.finishedAt;
    if (source.team1 !== undefined) {
      target.team1 = Team.clone(source.team1);
    }
    if (source.team2 !== undefined) {
      target.team2 = Team.clone(source.team2);
    }
    if (source.duels !== undefined) {
      target.duels = [];
      for (let duel of target.duels) {
        target.duels.push(Duel.clone(duel));
      }
    }
  }

  public static clone(data: Fight): Fight {
    const instance: Fight = new Fight();
    this.copy(data, instance);
    return instance;
  }

}
