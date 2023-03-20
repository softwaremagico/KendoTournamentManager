import {FightStatistics} from "./fight-statistics.model";
import {Element} from "./element";
import {RoleType} from "./role-type";

export class TournamentStatistics extends Element {

  public menNumber: number;

  public koteNumber: number;

  public doNumber: number;

  public tsukiNumber: number;

  public hansokuNumber: number;

  public ipponNumber: number;

  public fightStatistics: FightStatistics;

  public startedAt: Date;

  public finishedAt: Date;

  public numberOfTeams: number;

  public numberOfParticipants: Map<RoleType, number>;

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
    target.startedAt = source.startedAt;
    target.finishedAt = source.finishedAt;
    target.numberOfTeams = source.numberOfTeams;
    target.numberOfParticipants = new Map();
    Object.keys(source.numberOfParticipants).forEach(key => target.numberOfParticipants.set((key as RoleType), (source.numberOfParticipants as any)[key]));
  }

  public static clone(data: TournamentStatistics): TournamentStatistics {
    const instance: TournamentStatistics = new TournamentStatistics();
    this.copy(data, instance);
    return instance;
  }

  public numberOfParticipantsByRole(roleType: RoleType): number {
    if (this.numberOfParticipants.get(roleType)) {
      return this.numberOfParticipants.get(roleType)!;
    }
    return 0;
  }

}
