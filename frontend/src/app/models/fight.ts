import {Element} from "./element";
import {Tournament} from "./tournament";
import {Team} from "./team";
import {Duel} from "./duel";

/**
 * Client-side representation of a single fight between two {@link Team}s within a tournament.
 *
 * A fight consists of one {@link Duel} per active member pair (up to
 * {@code tournament.fightSize}). The fight is linked to a specific shiaijo and
 * a round level within the tournament bracket.
 */
export class Fight extends Element {
  /** The first (left / red) team competing in this fight. */
  public team1: Team;
  /** The second (right / white) team competing in this fight. */
  public team2: Team;
  /** The tournament this fight belongs to. */
  public tournament: Tournament;
  /** Zero-based index of the shiaijo (fighting area) where this fight takes place. */
  public shiaijo: number;
  /** Timestamp at which the fight was concluded. */
  public finishedAt: Date;
  /**
   * Round level within the bracket (0 = first round).
   * Higher values represent later knockout rounds.
   */
  public level: number;
  /**
   * Individual duels between paired members of the two teams.
   * The list is ordered by member position index.
   */
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
