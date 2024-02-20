import {Tournament} from "./tournament";
import {Team} from "./team";
import {Fight} from "./fight";
import {Duel} from "./duel";
import {Element} from "./element";

export class Group extends Element {
  tournament: Tournament;
  teams: Team[] = [];
  shiaijo: number;
  level: number;
  index: number;
  fights: Fight[] = [];
  numberOfWinners: number;
  unties: Duel[];

  public static override copy(source: Group, target: Group): void {
    Element.copy(source, target);
    if (source.tournament !== undefined) {
      target.tournament = Tournament.clone(source.tournament);
    }
    target.shiaijo = source.shiaijo;
    target.level = source.level;
    target.numberOfWinners = source.numberOfWinners;
    target.index = source.index;
    if (source.teams !== undefined) {
      target.teams = [];
      for (let team of target.teams) {
        target.teams.push(Team.clone(team));
      }
    }
    if (source.fights !== undefined) {
      target.fights = [];
      for (let fight of target.fights) {
        target.fights.push(Fight.clone(fight));
      }
    }
    if (source.unties !== undefined) {
      target.unties = [];
      for (let duel of target.unties) {
        target.unties.push(Duel.clone(duel));
      }
    }
  }

  public static clone(data: Group): Group {
    const instance: Group = new Group();
    this.copy(data, instance);
    return instance;
  }

  public isFinished(): boolean {
    for (const fight of this.fights) {
      for (const duel of fight.duels) {
        if (!duel.finished) {
          return false;
        }
      }
      for (const duel of this.unties) {
        if (!duel.finished) {
          return false;
        }
      }
    }
    return true;
  }

  public static isFinished(group: Group): boolean {
    for (const fight of group.fights) {
      for (const duel of fight.duels) {
        if (!duel.finished) {
          return false;
        }
      }
      for (const duel of group.unties) {
        if (!duel.finished) {
          return false;
        }
      }
    }
    return true;
  }
}
