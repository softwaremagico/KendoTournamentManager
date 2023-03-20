import {Element} from "./element";

export class FightStatistics extends Element {

  public menNumber: number;
  public koteNumber: number;
  public doNumber: number;
  public tsukiNumber: number;
  public hansokuNumber: number;
  public ipponNumber: number;
  fightsNumber: number;
  fightsByTeam?: number;
  duelsNumber: number;
  //In seconds.
  estimatedTime: number;
  averageTime: number;
  public fightsStartedAt: Date;
  public fightsFinishedAt: Date;

  public static override copy(source: FightStatistics, target: FightStatistics): void {
    Element.copy(source, target);
    target.menNumber = source.menNumber;
    target.koteNumber = source.koteNumber;
    target.doNumber = source.doNumber;
    target.tsukiNumber = source.tsukiNumber;
    target.hansokuNumber = source.hansokuNumber;
    target.ipponNumber = source.ipponNumber;
    target.fightsNumber = source.fightsNumber;
    target.fightsByTeam = source.fightsByTeam;
    target.duelsNumber = source.duelsNumber;
    target.estimatedTime = source.estimatedTime;
    target.averageTime = source.averageTime;
    target.fightsStartedAt = source.fightsStartedAt;
    target.fightsFinishedAt = source.fightsFinishedAt;
  }

  public static clone(data: FightStatistics): FightStatistics {
    const instance: FightStatistics = new FightStatistics();
    this.copy(data, instance);
    return instance;
  }

  public duelsDuration(): number | undefined {
    if (this.duelsNumber && this.averageTime) {
      return this.duelsNumber * this.averageTime;
    }
    return undefined;
  }
}
