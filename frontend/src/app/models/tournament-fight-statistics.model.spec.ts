import {TournamentFightStatistics} from './tournament-fight-statistics.model';

describe('TournamentFightStatistics', () => {
  const buildStats = (): TournamentFightStatistics => {
    const stats = new TournamentFightStatistics();
    stats.menNumber = 1;
    stats.koteNumber = 2;
    stats.doNumber = 3;
    stats.tsukiNumber = 4;
    stats.hansokuNumber = 5;
    stats.ipponNumber = 6;
    stats.fusenGachiNumber = 7;
    stats.fightsNumber = 8;
    stats.fightsByTeam = 2;
    stats.duelsNumber = 9;
    stats.estimatedTime = 100;
    stats.averageTime = 11;
    stats.fightsFinished = 4;
    stats.fightsStartedAt = new Date('2024-01-01T00:00:00Z');
    stats.fightsFinishedAt = new Date('2024-01-01T01:00:00Z');
    stats.faults = 3;
    return stats;
  };

  it('should copy all scalar fields and clone dates', () => {
    const source = buildStats();
    const target = new TournamentFightStatistics();

    TournamentFightStatistics.copy(source, target);

    expect(target.menNumber).toBe(1);
    expect(target.fightsNumber).toBe(8);
    expect(target.faults).toBe(3);
    expect(target.fightsStartedAt).toEqual(source.fightsStartedAt);
    expect(target.fightsStartedAt).not.toBe(source.fightsStartedAt);
    expect(target.fightsFinishedAt).not.toBe(source.fightsFinishedAt);
  });

  it('should clone tournament fight statistics', () => {
    const source = buildStats();

    const clone = TournamentFightStatistics.clone(source);

    expect(clone).not.toBe(source);
    expect(clone.averageTime).toBe(11);
    expect(clone.fightsFinished).toBe(4);
  });

  it('should calculate duels duration when duelsNumber and averageTime exist', () => {
    const stats = buildStats();

    expect(stats.duelsDuration()).toBe(99);
  });

  it('should return undefined duels duration when values are missing', () => {
    const stats = buildStats();
    stats.duelsNumber = 0;

    expect(stats.duelsDuration()).toBeUndefined();
  });
});

