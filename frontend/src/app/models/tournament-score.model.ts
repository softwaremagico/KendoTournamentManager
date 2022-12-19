import {ScoreType} from "./score-type";
import {Element} from "./element";

export class TournamentScore extends Element {
  public scoreType: ScoreType;
  public pointsByVictory?: number;
  public pointsByDraw?: number;

  constructor() {
    super();
    this.scoreType = ScoreType.EUROPEAN;
  }

  public static override copy(source: TournamentScore, target: TournamentScore): void {
    Element.copy(source, target);
    target.scoreType = source.scoreType;
    target.pointsByVictory = source.pointsByVictory;
    target.pointsByDraw = source.pointsByDraw;
  }

  public static clone(data: TournamentScore): TournamentScore {
    const instance: TournamentScore = new TournamentScore();
    this.copy(data, instance);
    return instance;
  }

}
