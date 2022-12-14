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

}
