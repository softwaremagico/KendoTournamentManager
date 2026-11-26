import {Team} from "./team";
import {SwissTieBreakRule} from "./swiss-tie-break-rule";

export class ScoreOfTeam {
  public team: Team;
  public wonFights: number;
  public drawFights: number;
  public wonDuels: number;
  public drawDuels: number;
  public untieDuels: number;
  public hits: number;
  public hitsLost: number;
  public sortingIndex: number;
  public swissTieBreakRuleUsed?: SwissTieBreakRule;
  public swissTieBreakValue?: number;

  public static copy(source: ScoreOfTeam, target: ScoreOfTeam): void {
    target.wonFights = source.wonFights;
    target.drawFights = source.drawFights;
    target.wonDuels = source.wonDuels;
    target.drawDuels = source.drawDuels;
    target.hits = source.hits;
    target.hitsLost = source.hitsLost;
    target.untieDuels = source.untieDuels;
    target.sortingIndex = source.sortingIndex;
    target.swissTieBreakRuleUsed = source.swissTieBreakRuleUsed;
    target.swissTieBreakValue = source.swissTieBreakValue;
    if (source.team !== undefined) {
      target.team = Team.clone(source.team);
    }
  }

  public static clone(data: ScoreOfTeam): ScoreOfTeam {
    const instance: ScoreOfTeam = new ScoreOfTeam();
    this.copy(data, instance);
    return instance;
  }
}
