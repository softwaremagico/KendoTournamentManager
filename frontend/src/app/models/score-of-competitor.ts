import {Participant} from "./participant";

export class ScoreOfCompetitor {
  public competitor: Participant;
  public wonDuels: number;
  public drawDuels: number;
  public hits: number;
  public hitsLost: number;
  public duelsDone: number;
  public wonFights: number;
  public drawFights: number;
  public untieDuels: number;
  public untieHits: number;
  public totalFights: number;

  public static copy(source: ScoreOfCompetitor, target: ScoreOfCompetitor): void {
    target.wonDuels = source.wonDuels;
    target.drawDuels = source.drawDuels;
    target.hits = source.hits;
    target.hitsLost = source.hitsLost;
    target.duelsDone = source.duelsDone;
    target.wonFights = source.wonFights;
    target.drawFights = source.drawFights;
    target.totalFights = source.totalFights;
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
