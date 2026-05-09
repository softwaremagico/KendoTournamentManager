import {ParticipantFightStatistics} from './participant-fight-statistics.model';

describe('ParticipantFightStatistics', () => {
  const buildStats = (): ParticipantFightStatistics => {
    const stats = new ParticipantFightStatistics();
    stats.menNumber = 1;
    stats.koteNumber = 2;
    stats.doNumber = 3;
    stats.tsukiNumber = 4;
    stats.hansokuNumber = 5;
    stats.ipponNumber = 6;
    stats.fusenGachiNumber = 7;
    stats.receivedMenNumber = 2;
    stats.receivedKoteNumber = 2;
    stats.receivedDoNumber = 2;
    stats.receivedTsukiNumber = 2;
    stats.receivedHansokuNumber = 2;
    stats.receivedIpponNumber = 2;
    stats.receivedFusenGachiNumber = 2;
    stats.duelsNumber = 10;
    stats.averageTime = 20;
    stats.averageWinTime = 30;
    stats.averageLostTime = 40;
    stats.totalDuelsTime = 200;
    stats.faults = 3;
    stats.receivedFaults = 4;
    stats.quickestHit = 5;
    stats.quickestReceivedHit = 6;
    stats.wonDuels = 7;
    stats.lostDuels = 8;
    stats.drawDuels = 9;
    return stats;
  };

  it('should copy all participant fight stats fields', () => {
    const source = buildStats();
    const target = new ParticipantFightStatistics();

    ParticipantFightStatistics.copy(source, target);

    expect(target.menNumber).toBe(1);
    expect(target.receivedHansokuNumber).toBe(2);
    expect(target.drawDuels).toBe(9);
    expect(target.quickestReceivedHit).toBe(6);
  });

  it('should clone participant fight stats', () => {
    const source = buildStats();

    const clone = ParticipantFightStatistics.clone(source);

    expect(clone).not.toBe(source);
    expect(clone.wonDuels).toBe(7);
  });

  it('should calculate duels duration when possible', () => {
    const stats = buildStats();

    expect(stats.duelsDuration()).toBe(200);
  });

  it('should return undefined duels duration when impossible', () => {
    const stats = buildStats();
    stats.averageTime = 0;

    expect(stats.duelsDuration()).toBeUndefined();
  });

  it('should calculate total hits', () => {
    const stats = buildStats();

    expect(stats.getTotalHits()).toBe(28);
  });

  it('should calculate total received hits', () => {
    const stats = buildStats();

    expect(stats.getTotalReceivedHits()).toBe(14);
  });
});

