import {TournamentFightStatistics} from "./tournament-fight-statistics.model";
import {Element} from "./element";
import {RoleType} from "./role-type";

export class TournamentStatistics extends Element {

  public tournamentId: number;

  public tournamentName: string;

  public tournamentFightStatistics: TournamentFightStatistics;

  public tournamentCreatedAt: Date;

  public tournamentLockedAt: Date;

  public tournamentFinishedAt: Date;

  public numberOfTeams: number;

  public teamSize: number;

  public numberOfParticipants: Map<RoleType, number>;

  public static override copy(source: TournamentStatistics, target: TournamentStatistics): void {
    if (source == undefined) {
      return undefined;
    }
    Element.copy(source, target);
    if (source.tournamentFightStatistics !== undefined) {
      target.tournamentFightStatistics = TournamentFightStatistics.clone(source.tournamentFightStatistics);
    }
    target.tournamentId = source.tournamentId;
    target.tournamentName = source.tournamentName;
    target.tournamentCreatedAt = source.tournamentCreatedAt;
    target.tournamentLockedAt = source.tournamentLockedAt;
    target.tournamentFinishedAt = source.tournamentFinishedAt;
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
    if (this.numberOfParticipants?.get(roleType)) {
      return this.numberOfParticipants.get(roleType)!;
    }
    return 0;
  }

}
