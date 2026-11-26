import {ParticipantStatistics} from './participant-statistics.model';
import {ParticipantFightStatistics} from './participant-fight-statistics.model';
import {RoleType} from './role-type';

describe('ParticipantStatistics', () => {
  const buildStats = (): ParticipantStatistics => {
    const stats = new ParticipantStatistics();
    stats.participantId = 1;
    stats.participantName = 'John Doe';
    stats.participantCreatedAt = new Date('2024-01-01T00:00:00Z');
    stats.tournaments = 4;
    stats.totalTournaments = 10;
    stats.participantFightStatistics = new ParticipantFightStatistics();
    stats.participantFightStatistics.menNumber = 2;
    stats.rolesPerformed = new Map<RoleType, number>([
      [RoleType.COMPETITOR, 8],
      [RoleType.REFEREE, 3]
    ]);
    return stats;
  };

  it('should copy participant statistics including nested fight stats', () => {
    const source = buildStats();
    const target = new ParticipantStatistics();

    ParticipantStatistics.copy(source, target);

    expect(target.participantId).toBe(1);
    expect(target.participantName).toBe('John Doe');
    expect(target.participantFightStatistics).toBeTruthy();
    expect(target.participantFightStatistics).not.toBe(source.participantFightStatistics);
  });

  it('should clone participant statistics', () => {
    const source = buildStats();

    const clone = ParticipantStatistics.clone(source);

    expect(clone).not.toBe(source);
    expect(clone.tournaments).toBe(4);
  });

  it('should return number of role performed when present', () => {
    const stats = buildStats();

    expect(stats.numberOfRolePerformed(RoleType.COMPETITOR)).toBe(8);
    expect(stats.numberOfRolePerformed(RoleType.REFEREE)).toBe(3);
  });

  it('should return zero for role not present', () => {
    const stats = buildStats();

    expect(stats.numberOfRolePerformed(RoleType.ORGANIZER)).toBe(0);
  });

  it('copy should return undefined when source is undefined', () => {
    const target = new ParticipantStatistics();

    const result = ParticipantStatistics.copy(undefined as any, target);

    expect(result).toBeUndefined();
  });
});

