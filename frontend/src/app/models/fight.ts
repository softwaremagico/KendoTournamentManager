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


}
