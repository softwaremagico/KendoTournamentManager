import {FightStatistics} from "./fight-statistics.model";
import {Element} from "./element";

export class TournamentStatistics extends Element {

  public menNumber: number;

  public koteNumber: number;

  public doNumber: number;

  public tsukiNumber: number;

  public hansokuNumber: number;

  public ipponNumber: number;

  public fightStatistics: FightStatistics;

  public static override copy(source: TournamentStatistics, target: TournamentStatistics): void {
    Element.copy(source, target);
    target.menNumber = source.menNumber;
    target.koteNumber = source.koteNumber;
    target.doNumber = source.doNumber;
    target.tsukiNumber = source.tsukiNumber;
    target.hansokuNumber = source.hansokuNumber;
    target.ipponNumber = source.ipponNumber;
    if (source.fightStatistics !== undefined) {
      target.fightStatistics = FightStatistics.clone(source.fightStatistics);
    }
  }

  public static clone(data: TournamentStatistics): TournamentStatistics {
    const instance: TournamentStatistics = new TournamentStatistics();
    this.copy(data, instance);
    return instance;
  }

}
