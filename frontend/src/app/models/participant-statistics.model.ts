import {Element} from "./element";
import {RoleType} from "./role-type";
import {ParticipantFightStatistics} from "./participant-fight-statistics.model";

export class ParticipantStatistics extends Element {

  public participantId: number;

  public participantName: string;

  public participantCreatedAt: Date;

  public participantFightStatistics: ParticipantFightStatistics;

  public tournaments: number;

  public totalTournaments: number;

  public rolesPerformed: Map<RoleType, number>;

  public static override copy(source: ParticipantStatistics, target: ParticipantStatistics): void {
    if (source == undefined) {
      return undefined;
    }
    Element.copy(source, target);
    if (source.participantFightStatistics !== undefined) {
      target.participantFightStatistics = ParticipantFightStatistics.clone(source.participantFightStatistics);
    }
    target.participantId = source.participantId;
    target.participantName = source.participantName;
    target.tournaments = source.tournaments;
    target.totalTournaments = source.totalTournaments;
    target.participantCreatedAt = source.participantCreatedAt;
    target.rolesPerformed = new Map();
    Object.keys(source.rolesPerformed).forEach(key => target.rolesPerformed.set((key as RoleType), (source.rolesPerformed as any)[key]));
  }

  public static clone(data: ParticipantStatistics): ParticipantStatistics {
    const instance: ParticipantStatistics = new ParticipantStatistics();
    this.copy(data, instance);
    return instance;
  }

  public numberOfRolePerformed(roleType: RoleType): number {
    if (this.rolesPerformed?.get(roleType)) {
      return this.rolesPerformed.get(roleType)!;
    }
    return 0;
  }

}
