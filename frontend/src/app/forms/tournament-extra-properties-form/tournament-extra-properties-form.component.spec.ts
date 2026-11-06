import {of, Subject} from 'rxjs';
import {TournamentExtraPropertiesFormComponent} from './tournament-extra-properties-form.component';
import {RbacService} from '../../services/rbac/rbac.service';
import {TranslocoService} from '@jsverse/transloco';
import {TournamentExtendedPropertiesService} from '../../services/tournament-extended-properties.service';
import {MessageService} from '../../services/message.service';
import {Tournament} from '../../models/tournament';
import {TournamentScore} from '../../models/tournament-score.model';
import {TournamentType} from '../../models/tournament-type';
import {TournamentExtraPropertyKey} from '../../models/tournament-extra-property-key';
import {DrawResolution} from '../../models/draw-resolution';
import {LeagueFightsOrder} from '../../models/league-fights-order';
import {SwissTieBreakRule} from '../../models/swiss-tie-break-rule';

describe('TournamentExtraPropertiesFormComponent', () => {
  let component: TournamentExtraPropertiesFormComponent;
  let rbacServiceSpy: jasmine.SpyObj<RbacService>;
  let translocoServiceSpy: jasmine.SpyObj<TranslocoService>;
  let tournamentExtendedPropertiesServiceSpy: jasmine.SpyObj<TournamentExtendedPropertiesService>;
  let messageServiceSpy: jasmine.SpyObj<MessageService>;

  const buildTournament = (type: TournamentType = TournamentType.CHAMPIONSHIP): Tournament => {
    const t = new Tournament();
    t.id = 1;
    t.name = 'Test Tournament';
    t.type = type;
    t.tournamentScore = new TournamentScore();
    return t;
  };

  beforeEach(() => {
    rbacServiceSpy = jasmine.createSpyObj('RbacService', ['isAllowed']);
    translocoServiceSpy = jasmine.createSpyObj('TranslocoService', ['selectTranslate', 'translate']);
    tournamentExtendedPropertiesServiceSpy = jasmine.createSpyObj('TournamentExtendedPropertiesService', ['getByTournament', 'update']);
    messageServiceSpy = jasmine.createSpyObj('MessageService', ['infoMessage', 'errorMessage']);

    rbacServiceSpy.isAllowed.and.returnValue(true);
    translocoServiceSpy.selectTranslate.and.returnValue(of('translated'));
    translocoServiceSpy.translate.and.returnValue('translated hint');
    tournamentExtendedPropertiesServiceSpy.getByTournament.and.returnValue(of([]));
    tournamentExtendedPropertiesServiceSpy.update.and.returnValue(of({}) as any);

    component = new TournamentExtraPropertiesFormComponent(
      rbacServiceSpy,
      translocoServiceSpy,
      tournamentExtendedPropertiesServiceSpy,
      messageServiceSpy
    );

    component.tournament = buildTournament();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should set default properties in constructor', () => {
    expect(component.areFightsMaximized).toBeFalse();
    expect(component.selectedDrawResolution).toBe(DrawResolution.BOTH_ELIMINATED);
    expect(component.firstInFirstOut).toBeTrue();
    expect(component.avoidDuplicatedFights).toBeTrue();
    expect(component.resolveOddFightsAsap).toBeTrue();
    expect(component.challengeDistance).toBe(3);
  });

  it('should initialize senbatsu challenge distance values in constructor', () => {
    expect((component as any).senbatsuChallengeDistance.length).toBe(4);
    expect((component as any).senbatsuChallengeDistance[0].value).toBe('2');
  });

  it('should initialize draw resolution translated values in constructor', () => {
    expect((component as any).drawResolutionValues.length).toBe(3);
  });

  it('should set capability flags in ngOnInit based on tournament type', () => {
    component.tournament = buildTournament(TournamentType.CHAMPIONSHIP);

    component.ngOnInit();

    expect(component.canMaximizeFights).toBeTrue();
    expect(component.needsFifoWinner).toBeTrue();
    expect(component.canResolveOddFightsAsap).toBeTrue();
    expect(component.canAvoidDuplicatedFights).toBeFalse();
  });

  it('should set senbatsu flags correctly in ngOnInit', () => {
    component.tournament = buildTournament(TournamentType.SENBATSU);

    component.ngOnInit();

    expect(component.canSelectChallengeDistance).toBeTrue();
    expect(component.needsDrawResolution).toBeFalse();
  });

  it('should load persisted properties in ngOnInit', () => {
    tournamentExtendedPropertiesServiceSpy.getByTournament.and.returnValue(of([
      { propertyKey: TournamentExtraPropertyKey.KING_DRAW_RESOLUTION, propertyValue: DrawResolution.NEWEST_ELIMINATED } as any,
      { propertyKey: TournamentExtraPropertyKey.MAXIMIZE_FIGHTS, propertyValue: 'true' } as any,
      { propertyKey: TournamentExtraPropertyKey.LEAGUE_FIGHTS_ORDER_GENERATION, propertyValue: LeagueFightsOrder.LIFO } as any,
      { propertyKey: TournamentExtraPropertyKey.AVOID_DUPLICATES, propertyValue: 'false' } as any,
      { propertyKey: TournamentExtraPropertyKey.ODD_FIGHTS_RESOLVED_ASAP, propertyValue: 'false' } as any,
      { propertyKey: TournamentExtraPropertyKey.SENBATSU_CHALLENGE_DISTANCE, propertyValue: '4' } as any
    ]));

    component.ngOnInit();

    expect(component.selectedDrawResolution).toBe(DrawResolution.NEWEST_ELIMINATED);
    expect(component.areFightsMaximized).toBeTrue();
    expect(component.firstInFirstOut).toBeFalse();
    expect(component.avoidDuplicatedFights).toBeFalse();
    expect(component.resolveOddFightsAsap).toBeFalse();
    expect(component.challengeDistance).toBe(4);
    expect(component.propertiesLoaded).toBeTrue();
  });

  it('should wait for existing tournament properties to load before allowing a save', () => {
    const properties = new Subject<any[]>();
    tournamentExtendedPropertiesServiceSpy.getByTournament.and.returnValue(properties);

    component.ngOnInit();

    expect(component.propertiesLoaded).toBeFalse();
    properties.next([]);
    expect(component.propertiesLoaded).toBeTrue();
    properties.complete();
  });

  it('should allow a new tournament form to save without loading persisted properties', () => {
    component.tournament = buildTournament();
    component.tournament.id = undefined;

    component.ngOnInit();

    expect(component.propertiesLoaded).toBeTrue();
    expect(tournamentExtendedPropertiesServiceSpy.getByTournament).not.toHaveBeenCalled();
  });

  it('should use default challengeDistance when persisted value is not a number', () => {
    tournamentExtendedPropertiesServiceSpy.getByTournament.and.returnValue(of([
      { propertyKey: TournamentExtraPropertyKey.SENBATSU_CHALLENGE_DISTANCE, propertyValue: 'NaN' } as any
    ]));

    component.ngOnInit();

    expect(component.challengeDistance).toBe(3);
  });

  it('should update visible property groups immediately when the tournament type changes', () => {
    component.tournament = buildTournament(TournamentType.CHAMPIONSHIP);
    expect(component.canMaximizeFights).toBeTrue();
    expect(component.canConfigureSwiss).toBeFalse();

    component.tournament.type = TournamentType.SWISS;

    expect(component.canMaximizeFights).toBeFalse();
    expect(component.canConfigureSwiss).toBeTrue();
  });

  it('should save league fights order as FIFO or LIFO with the main form', () => {
    component.tournament = buildTournament(TournamentType.LEAGUE);
    component.firstInFirstOut = true;
    component.saveProperties(component.tournament).subscribe();
    expect(tournamentExtendedPropertiesServiceSpy.update.calls.mostRecent().args[0].propertyValue)
      .toBe(LeagueFightsOrder.FIFO);

    tournamentExtendedPropertiesServiceSpy.update.calls.reset();
    component.firstInFirstOut = false;
    component.saveProperties(component.tournament).subscribe();
    expect(tournamentExtendedPropertiesServiceSpy.update.calls.mostRecent().args[0].propertyValue)
      .toBe(LeagueFightsOrder.LIFO);
  });

  it('should save properties for the active tournament type only', () => {
    component.tournament = buildTournament(TournamentType.SWISS);
    component.saveProperties(component.tournament).subscribe();

    const savedKeys = tournamentExtendedPropertiesServiceSpy.update.calls.allArgs()
      .map(([property]) => property.propertyKey);
    expect(savedKeys).toEqual([
      TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS,
      TournamentExtraPropertyKey.SWISS_TIE_BREAK_RULE
    ]);
  });

  it('should save plain property values with the main form', () => {
    component.tournament = buildTournament(TournamentType.CHAMPIONSHIP);
    component.areFightsMaximized = true;
    component.saveProperties(component.tournament).subscribe();

    const property = tournamentExtendedPropertiesServiceSpy.update.calls.allArgs()
      .map(([item]) => item)
      .find(item => item.propertyKey === TournamentExtraPropertyKey.MAXIMIZE_FIGHTS);
    expect(property?.propertyValue).toBe('true');
  });

  it('should surface a failed property save instead of completing successfully', () => {
    component.tournament = buildTournament(TournamentType.SWISS);
    tournamentExtendedPropertiesServiceSpy.update.and.returnValue(of(undefined) as any);
    const errorSpy = jasmine.createSpy('error');

    component.saveProperties(component.tournament).subscribe({error: errorSpy});

    expect(errorSpy).toHaveBeenCalled();
  });

  // ─── Swiss ───────────────────────────────────────────────────────────────────

  describe('Swiss tournament', () => {

    it('should set canConfigureSwiss to true only for SWISS type', () => {
      component.tournament = buildTournament(TournamentType.SWISS);
      component.ngOnInit();
      expect(component.canConfigureSwiss).toBeTrue();
    });

    it('should set canConfigureSwiss to false for non-Swiss types', () => {
      for (const type of [TournamentType.LEAGUE, TournamentType.CHAMPIONSHIP, TournamentType.SENBATSU, TournamentType.LOOP]) {
        component.tournament = buildTournament(type);
        component.ngOnInit();
        expect(component.canConfigureSwiss).withContext(`type ${type}`).toBeFalse();
      }
    });

    it('should set Swiss default properties in constructor', () => {
      expect(component.swissRounds).toBeNull();
      expect(component.swissTieBreakRule).toBe(SwissTieBreakRule.BUCHHOLZ);
      expect(component.swissAvoidRepeatedPairings).toBeTrue();
    });

    it('should initialize swissTieBreakRuleValues with all 5 rules in constructor', () => {
      expect((component as any).swissTieBreakRuleValues.length).toBe(5);
      const values: string[] = (component as any).swissTieBreakRuleValues.map((v: any) => v.value);
      expect(values).toContain(SwissTieBreakRule.BUCHHOLZ);
      expect(values).toContain(SwissTieBreakRule.MEDIAN_BUCHHOLZ);
      expect(values).toContain(SwissTieBreakRule.SONNEBORN_BERGER);
      expect(values).toContain(SwissTieBreakRule.DIRECT_ENCOUNTER);
      expect(values).toContain(SwissTieBreakRule.POINT_DIFFERENTIAL);
    });

    it('should load persisted Swiss properties in ngOnInit', () => {
      tournamentExtendedPropertiesServiceSpy.getByTournament.and.returnValue(of([
        { propertyKey: TournamentExtraPropertyKey.SWISS_TIE_BREAK_RULE, propertyValue: SwissTieBreakRule.SONNEBORN_BERGER } as any,
        { propertyKey: TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS, propertyValue: 'false' } as any,
        { propertyKey: TournamentExtraPropertyKey.SWISS_ROUNDS, propertyValue: '5' } as any
      ]));

      component.tournament = buildTournament(TournamentType.SWISS);
      component.ngOnInit();

      expect(component.swissTieBreakRule).toBe(SwissTieBreakRule.SONNEBORN_BERGER);
      expect(component.swissAvoidRepeatedPairings).toBeFalse();
      expect(component.swissRounds).toBe(5);
    });

    it('should use default tieBreakRule when persisted value is unknown', () => {
      tournamentExtendedPropertiesServiceSpy.getByTournament.and.returnValue(of([
        { propertyKey: TournamentExtraPropertyKey.SWISS_TIE_BREAK_RULE, propertyValue: 'UNKNOWN_RULE' } as any
      ]));

      component.tournament = buildTournament(TournamentType.SWISS);
      component.ngOnInit();

      expect(component.swissTieBreakRule).toBe(SwissTieBreakRule.BUCHHOLZ);
    });

    it('should set swissRounds to null when persisted value is not a number', () => {
      tournamentExtendedPropertiesServiceSpy.getByTournament.and.returnValue(of([
        { propertyKey: TournamentExtraPropertyKey.SWISS_ROUNDS, propertyValue: 'NaN' } as any
      ]));

      component.tournament = buildTournament(TournamentType.SWISS);
      component.ngOnInit();

      expect(component.swissRounds).toBeNull();
    });

    it('should persist SWISS_TIE_BREAK_RULE on save', () => {
      component.tournament = buildTournament(TournamentType.SWISS);
      component.swissTieBreakRule = SwissTieBreakRule.MEDIAN_BUCHHOLZ;
      component.saveProperties(component.tournament).subscribe();

      const property = tournamentExtendedPropertiesServiceSpy.update.calls.allArgs()
        .map(([item]) => item)
        .find(item => item.propertyKey === TournamentExtraPropertyKey.SWISS_TIE_BREAK_RULE);
      expect(property?.propertyValue).toBe(SwissTieBreakRule.MEDIAN_BUCHHOLZ);
    });

    it('should persist SWISS_AVOID_REPEATED_PAIRINGS on save', () => {
      component.tournament = buildTournament(TournamentType.SWISS);
      component.swissAvoidRepeatedPairings = false;
      component.saveProperties(component.tournament).subscribe();

      const property = tournamentExtendedPropertiesServiceSpy.update.calls.allArgs()
        .map(([item]) => item)
        .find(item => item.propertyKey === TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS);
      expect(property?.propertyValue).toBe('false');
    });

    it('should persist SWISS_ROUNDS on save', () => {
      component.tournament = buildTournament(TournamentType.SWISS);
      component.swissRounds = 7;
      component.saveProperties(component.tournament).subscribe();

      const property = tournamentExtendedPropertiesServiceSpy.update.calls.allArgs()
        .map(([item]) => item)
        .find(item => item.propertyKey === TournamentExtraPropertyKey.SWISS_ROUNDS);
      expect(property?.propertyValue).toBe('7');
    });

    it('should clear SWISS_ROUNDS when left empty', () => {
      component.tournament = buildTournament(TournamentType.SWISS);
      component.swissRounds = 5;
      component.saveProperties(component.tournament).subscribe();
      tournamentExtendedPropertiesServiceSpy.update.calls.reset();

      component.swissRounds = null;
      component.saveProperties(component.tournament).subscribe();

      const property = tournamentExtendedPropertiesServiceSpy.update.calls.allArgs()
        .map(([item]) => item)
        .find(item => item.propertyKey === TournamentExtraPropertyKey.SWISS_ROUNDS);
      expect(property?.propertyValue).toBe('');
    });

    it('should not set other tournament type flags when SWISS', () => {
      component.tournament = buildTournament(TournamentType.SWISS);
      component.ngOnInit();

      expect(component.canMaximizeFights).toBeFalse();
      expect(component.needsDrawResolution).toBeFalse();
      expect(component.canSelectChallengeDistance).toBeFalse();
      expect(component.needsFifoWinner).toBeFalse();
      expect(component.canAvoidDuplicatedFights).toBeFalse();
      expect(component.canResolveOddFightsAsap).toBeFalse();
    });

    it('should load each Swiss tieBreakRule value correctly', () => {
      for (const rule of SwissTieBreakRule.toArray()) {
        tournamentExtendedPropertiesServiceSpy.getByTournament.and.returnValue(of([
          { propertyKey: TournamentExtraPropertyKey.SWISS_TIE_BREAK_RULE, propertyValue: rule } as any
        ]));
        component.tournament = buildTournament(TournamentType.SWISS);
        component.ngOnInit();
        expect(component.swissTieBreakRule).withContext(`rule ${rule}`).toBe(rule);
      }
    });

  });
});
