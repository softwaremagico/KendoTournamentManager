import {Element} from "./element";

export class FightStatistics extends Element {

  fightsNumber: number;
  fightsByTeam?: number;
  duelsNumber: number;
  //In seconds.
  time: number;

  public static override copy(source: FightStatistics, target: FightStatistics): void {
    Element.copy(source, target);
    target.fightsNumber = source.fightsNumber;
    target.fightsByTeam = source.fightsByTeam;
    target.duelsNumber = source.duelsNumber;
    target.time = source.time;
  }

  public static clone(data: FightStatistics): FightStatistics {
    const instance: FightStatistics = new FightStatistics();
    this.copy(data, instance);
    return instance;
  }
}
