import { TournamentStatistics } from './tournament-statistics.model';
import { TournamentFightStatistics } from './tournament-fight-statistics.model';
import { RoleType } from './role-type';

describe('TournamentStatistics', () => {
  const buildStats = (): TournamentStatistics => {
    const stats = new TournamentStatistics();
    stats.tournamentId = 7;
    stats.tournamentName = 'League 2024';
    stats.tournamentCreatedAt = new Date('2024-01-01T00:00:00Z');
    stats.tournamentLockedAt = new Date('2024-01-02T00:00:00Z');
    stats.tournamentFinishedAt = new Date('2024-01-03T00:00:00Z');
    stats.numberOfTeams = 12;
    stats.teamSize = 3;
    stats.tournamentFightStatistics = new TournamentFightStatistics();
    stats.tournamentFightStatistics.menNumber = 9;
    stats.numberOfParticipants = new Map<RoleType, number>([
      [RoleType.COMPETITOR, 36],
      [RoleType.REFEREE, 4]
    ]);
    return stats;
  };

  it('should copy tournament statistics including nested fight stats', () => {
    const source = buildStats();
    const target = new TournamentStatistics();

    TournamentStatistics.copy(source, target);

    expect(target.tournamentId).toBe(7);
    expect(target.tournamentName).toBe('League 2024');
    expect(target.tournamentFightStatistics).toBeTruthy();
    expect(target.tournamentFightStatistics).not.toBe(source.tournamentFightStatistics);
  });

  it('should clone tournament statistics', () => {
    const source = buildStats();

    const clone = TournamentStatistics.clone(source);

    expect(clone).not.toBe(source);
    expect(clone.numberOfTeams).toBe(12);
  });

  it('should return participants by role when present', () => {
    const stats = buildStats();

    expect(stats.numberOfParticipantsByRole(RoleType.COMPETITOR)).toBe(36);
    expect(stats.numberOfParticipantsByRole(RoleType.REFEREE)).toBe(4);
  });

  it('should return zero for participant role not present', () => {
    const stats = buildStats();

    expect(stats.numberOfParticipantsByRole(RoleType.ORGANIZER)).toBe(0);
  });

  it('copy should return undefined when source is undefined', () => {
    const target = new TournamentStatistics();

    const result = TournamentStatistics.copy(undefined as any, target);

    expect(result).toBeUndefined();
  });
});

