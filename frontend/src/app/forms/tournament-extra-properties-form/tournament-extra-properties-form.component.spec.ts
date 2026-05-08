import { of } from 'rxjs';
import { TournamentExtraPropertiesFormComponent } from './tournament-extra-properties-form.component';
import { RbacService } from '../../services/rbac/rbac.service';
import { TranslocoService } from '@ngneat/transloco';
import { TournamentExtendedPropertiesService } from '../../services/tournament-extended-properties.service';
import { MessageService } from '../../services/message.service';
import { Tournament } from '../../models/tournament';
import { TournamentScore } from '../../models/tournament-score.model';
import { TournamentType } from '../../models/tournament-type';
import { TournamentExtraPropertyKey } from '../../models/tournament-extra-property-key';
import { DrawResolution } from '../../models/draw-resolution';
import { LeagueFightsOrder } from '../../models/league-fights-order';

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
  });

  it('should use default challengeDistance when persisted value is not a number', () => {
    tournamentExtendedPropertiesServiceSpy.getByTournament.and.returnValue(of([
      { propertyKey: TournamentExtraPropertyKey.SENBATSU_CHALLENGE_DISTANCE, propertyValue: 'NaN' } as any
    ]));

    component.ngOnInit();

    expect(component.challengeDistance).toBe(3);
  });

  it('should convert league fights order true to FIFO on save', () => {
    component.onSave(TournamentExtraPropertyKey.LEAGUE_FIGHTS_ORDER_GENERATION, 'true');

    const updateArg = tournamentExtendedPropertiesServiceSpy.update.calls.mostRecent().args[0];
    expect(updateArg.propertyValue).toBe(LeagueFightsOrder.FIFO);
  });

  it('should convert league fights order false to LIFO on save', () => {
    component.onSave(TournamentExtraPropertyKey.LEAGUE_FIGHTS_ORDER_GENERATION, 'false');

    const updateArg = tournamentExtendedPropertiesServiceSpy.update.calls.mostRecent().args[0];
    expect(updateArg.propertyValue).toBe(LeagueFightsOrder.LIFO);
  });

  it('should persist plain property values without conversion on save', () => {
    component.onSave(TournamentExtraPropertyKey.MAXIMIZE_FIGHTS, 'true');

    const updateArg = tournamentExtendedPropertiesServiceSpy.update.calls.mostRecent().args[0];
    expect(updateArg.propertyKey).toBe(TournamentExtraPropertyKey.MAXIMIZE_FIGHTS);
    expect(updateArg.propertyValue).toBe('true');
  });
});

