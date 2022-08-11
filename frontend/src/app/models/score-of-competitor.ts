import {Participant} from "./participant";

export class ScoreOfCompetitor {
  public competitor: Participant;
  public wonDuels: number;
  public drawDuels: number;
  public hits: number;
  public duelsDone: number;
  public fightsWon: number;
  public fightsDraw: number;

  public static copy(source: ScoreOfCompetitor, target: ScoreOfCompetitor): void {
    target.wonDuels = source.wonDuels;
    target.drawDuels = source.drawDuels;
    target.hits = source.hits;
    target.duelsDone = source.duelsDone;
    target.fightsWon = source.fightsWon;
    target.fightsDraw = source.fightsDraw;
    if (source.competitor !== undefined) {
      target.competitor = Participant.clone(source.competitor);
    }
  }

  public static clone(data: ScoreOfCompetitor): ScoreOfCompetitor {
    const instance: ScoreOfCompetitor = new ScoreOfCompetitor();
    this.copy(data, instance);
    return instance;
  }
}
