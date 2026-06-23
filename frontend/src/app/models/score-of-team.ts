import {Team} from "./team";

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

  public static copy(source: ScoreOfTeam, target: ScoreOfTeam): void {
    target.wonFights = source.wonFights;
    target.drawFights = source.drawFights;
    target.wonDuels = source.wonDuels;
    target.drawDuels = source.drawDuels;
    target.hits = source.hits;
    target.hitsLost = source.hitsLost;
    target.untieDuels = source.untieDuels;
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
