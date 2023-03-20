import {FightStatistics} from "./fight-statistics.model";
import {Element} from "./element";
import {RoleType} from "./role-type";

export class TournamentStatistics extends Element {

  public tournamentId: number;

  public fightStatistics: FightStatistics;

  public tournamentCreatedAt: Date;

  public tournamentLockedAt: Date;

  public numberOfTeams: number;

  public teamSize: number;

  public numberOfParticipants: Map<RoleType, number>;

  public static override copy(source: TournamentStatistics, target: TournamentStatistics): void {
    Element.copy(source, target);
    if (source.fightStatistics !== undefined) {
      target.fightStatistics = FightStatistics.clone(source.fightStatistics);
    }
    target.tournamentCreatedAt = source.tournamentCreatedAt;
    target.tournamentLockedAt = source.tournamentLockedAt;
    target.numberOfTeams = source.numberOfTeams;
    target.teamSize = source.teamSize;
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
