import {DrawResolution} from './draw-resolution';
import {SwissTieBreakRule} from './swiss-tie-break-rule';
import {TournamentExtraPropertyKey} from './tournament-extra-property-key';

describe('TournamentExtraPropertyKey', () => {
  it('provides defaults for every configurable property', () => {
    expect(TournamentExtraPropertyKey.getDefaultMaximizedFights()).toBeFalse();
    expect(TournamentExtraPropertyKey.getDefaultKingDrawResolutions()).toBe(DrawResolution.BOTH_ELIMINATED);
    expect(TournamentExtraPropertyKey.getDefaultLeagueFightsOrderGeneration()).toBeTrue();
    expect(TournamentExtraPropertyKey.avoidDuplicateFightsGeneration()).toBeTrue();
    expect(TournamentExtraPropertyKey.oddFightsResolvedAsap()).toBeTrue();
    expect(TournamentExtraPropertyKey.senbatsuChallengeDistance()).toBe(3);
    expect(TournamentExtraPropertyKey.swissDefaultRounds()).toBeNull();
    expect(TournamentExtraPropertyKey.swissDefaultTieBreakRule()).toBe(SwissTieBreakRule.BUCHHOLZ);
    expect(TournamentExtraPropertyKey.swissDefaultAvoidRepeatedPairings()).toBeTrue();
  });

  it('resolves enum keys and values', () => {
    expect(TournamentExtraPropertyKey.getKeys()).toHaveSize(11);
    expect(TournamentExtraPropertyKey.toArray()).toContain(TournamentExtraPropertyKey.SWISS_ROUNDS);
    expect(TournamentExtraPropertyKey.getByKey('SWISS_ROUNDS')).toBe('SWISS_ROUNDS');
    expect(TournamentExtraPropertyKey.getByKey('UNKNOWN')).toBeUndefined();
    expect(TournamentExtraPropertyKey.getEnumKeyByEnumValue(TournamentExtraPropertyKey as any, 'SWISS_ROUNDS')).toBe('SWISS_ROUNDS');
    expect(TournamentExtraPropertyKey.getEnumKeyByEnumValue(TournamentExtraPropertyKey as any, 'UNKNOWN')).toBeNull();
  });
});
