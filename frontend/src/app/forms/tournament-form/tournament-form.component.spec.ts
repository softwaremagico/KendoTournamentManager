import {of, throwError} from 'rxjs';
import {BiitSnackbarService, NotificationType} from '@biit-solutions/wizardry-theme/info';
import {TournamentFormComponent} from './tournament-form.component';
import {RbacService} from '../../services/rbac/rbac.service';
import {TranslocoService} from '@jsverse/transloco';
import {TournamentService} from '../../services/tournament.service';
import {FightService} from '../../services/fight.service';
import {Tournament} from '../../models/tournament';
import {TournamentScore} from '../../models/tournament-score.model';
import {TournamentType} from '../../models/tournament-type';
import {TournamentFormValidationFields} from './tournament-form-validation-fields';

describe('TournamentFormComponent', () => {
  let component: TournamentFormComponent;
  let rbacServiceSpy: jasmine.SpyObj<RbacService>;
  let translocoServiceSpy: jasmine.SpyObj<TranslocoService>;
  let biitSnackbarServiceSpy: jasmine.SpyObj<BiitSnackbarService>;
  let tournamentServiceSpy: jasmine.SpyObj<TournamentService>;
  let fightServiceSpy: jasmine.SpyObj<FightService>;
  let extraPropertiesFormSpy: jasmine.SpyObj<{ saveProperties: (tournament: Tournament) => any }>;

  const buildTournament = (id?: number): Tournament => {
    const t = new Tournament();
    t.id = id;
    t.name = 'Summer Cup';
    t.type = TournamentType.LEAGUE;
    t.shiaijos = 2;
    t.teamSize = 3;
    t.fightSize = 2;
    t.duelsDuration = 180;
    t.tournamentScore = new TournamentScore();
    return t;
  };

  beforeEach(() => {
    rbacServiceSpy = jasmine.createSpyObj('RbacService', ['isAllowed']);
    translocoServiceSpy = jasmine.createSpyObj('TranslocoService', ['selectTranslate', 'translate']);
    biitSnackbarServiceSpy = jasmine.createSpyObj('BiitSnackbarService', ['showNotification']);
    tournamentServiceSpy = jasmine.createSpyObj('TournamentService', ['add', 'update']);
    fightServiceSpy = jasmine.createSpyObj('FightService', ['getFromTournament']);
    extraPropertiesFormSpy = jasmine.createSpyObj('TournamentExtraPropertiesFormComponent', ['saveProperties']);

    rbacServiceSpy.isAllowed.and.returnValue(true);
    translocoServiceSpy.selectTranslate.and.returnValue(of('translated'));
    translocoServiceSpy.translate.and.returnValue('translated');
    fightServiceSpy.getFromTournament.and.returnValue(of([]));
    extraPropertiesFormSpy.saveProperties.and.returnValue(of(undefined));

    component = new TournamentFormComponent(
      rbacServiceSpy,
      translocoServiceSpy,
      biitSnackbarServiceSpy,
      tournamentServiceSpy,
      fightServiceSpy
    );

    component.tournament = buildTournament(1);
    (component as any).extraPropertiesForm = extraPropertiesFormSpy;
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize translated lists on ngOnInit', () => {
    component.ngOnInit();

    expect((component as any).translatedTypes.length).toBeGreaterThan(0);
    expect((component as any).translatedScores.length).toBeGreaterThan(0);
    expect((component as any).translatedDuration.length).toBe(13);
  });

  it('should check if tournament started on ngOnInit for existing tournament', () => {
    fightServiceSpy.getFromTournament.and.returnValue(of([{} as any]));

    component.ngOnInit();

    expect(fightServiceSpy.getFromTournament).toHaveBeenCalledWith(component.tournament);
    expect(component.tournamentWithTeams).toBeTrue();
  });

  it('should set tournamentWithTeams to false for new tournament', () => {
    component.tournament = buildTournament(undefined);

    component.ngOnInit();

    expect(component.tournamentWithTeams).toBeFalse();
  });

  it('should return minutes and seconds correctly', () => {
    expect(component.getMinutes(185)).toBe(3);
    expect(component.getSeconds(185)).toBe(5);
  });

  it('should validate successfully with valid tournament', () => {
    const valid = (component as any).validate();

    expect(valid).toBeTrue();
    expect((component as any).errors.size).toBe(0);
  });

  it('should fail validation when name is empty', () => {
    component.tournament.name = '';

    const valid = (component as any).validate();

    expect(valid).toBeFalse();
    expect((component as any).errors.has(TournamentFormValidationFields.NAME_ERRORS)).toBeTrue();
  });

  it('should fail validation when shiaijos is out of range', () => {
    component.tournament.shiaijos = 11;

    const valid = (component as any).validate();

    expect(valid).toBeFalse();
    expect((component as any).errors.has(TournamentFormValidationFields.SHIAIJO_ERRORS)).toBeTrue();
  });

  it('should fail validation when teamSize is smaller than fightSize', () => {
    component.tournament.teamSize = 1;
    component.tournament.fightSize = 2;

    const valid = (component as any).validate();

    expect(valid).toBeFalse();
    expect((component as any).errors.get(TournamentFormValidationFields.TEAM_ERRORS)).toBe('translated');
    expect((component as any).errors.get(TournamentFormValidationFields.FIGHT_ERRORS)).toBe('translated');
  });

  it('should call update on save for existing tournament', () => {
    tournamentServiceSpy.update.and.returnValue(of(component.tournament));
    spyOn(component.saved, 'emit');

    component.onSave();

    expect(tournamentServiceSpy.update).toHaveBeenCalledWith(component.tournament);
    expect(extraPropertiesFormSpy.saveProperties).toHaveBeenCalledWith(component.tournament);
    expect(component.saved.emit).toHaveBeenCalled();
  });

  it('should call add on save for new tournament', () => {
    component.tournament = buildTournament(undefined);
    const savedTournament = buildTournament(2);
    tournamentServiceSpy.add.and.returnValue(of(savedTournament));
    spyOn(component.saved, 'emit');

    component.onSave();

    expect(tournamentServiceSpy.add).toHaveBeenCalledWith(component.tournament);
    expect(component.tournament.id).toBe(2);
    expect(extraPropertiesFormSpy.saveProperties).toHaveBeenCalledWith(savedTournament);
    expect(component.saved.emit).toHaveBeenCalled();
  });

  it('should save tournament properties only after the tournament has been saved', () => {
    const calls: string[] = [];
    const savedTournament = buildTournament(2);
    tournamentServiceSpy.add.and.callFake(tournament => {
      calls.push('tournament');
      return of(savedTournament);
    });
    extraPropertiesFormSpy.saveProperties.and.callFake(() => {
      calls.push('properties');
      return of(undefined);
    });
    component.tournament = buildTournament(undefined);

    component.onSave();

    expect(calls).toEqual(['tournament', 'properties']);
  });

  it('should not save tournament properties if saving the tournament fails', () => {
    tournamentServiceSpy.add.and.returnValue(throwError(() => new Error('save failed')));
    component.tournament = buildTournament(undefined);

    component.onSave();

    expect(extraPropertiesFormSpy.saveProperties).not.toHaveBeenCalled();
  });

  it('should retain the created tournament id if saving its properties fails', () => {
    const savedTournament = buildTournament(42);
    component.tournament = buildTournament(undefined);
    tournamentServiceSpy.add.and.returnValue(of(savedTournament));
    extraPropertiesFormSpy.saveProperties.and.returnValue(throwError(() => new Error('property save failed')));
    spyOn(component.saved, 'emit');

    component.onSave();

    expect(component.tournament.id).toBe(42);
    expect(component.saved.emit).not.toHaveBeenCalled();
    tournamentServiceSpy.update.and.returnValue(of(component.tournament));
    extraPropertiesFormSpy.saveProperties.and.returnValue(of(undefined));
    component.onSave();

    expect(tournamentServiceSpy.add).toHaveBeenCalledTimes(1);
    expect(tournamentServiceSpy.update).toHaveBeenCalledWith(component.tournament);
  });

  it('should show warning and stop save when validation fails', () => {
    component.tournament.name = '';

    component.onSave();

    expect(biitSnackbarServiceSpy.showNotification).toHaveBeenCalledWith('translated', NotificationType.WARNING);
    expect(tournamentServiceSpy.add).not.toHaveBeenCalled();
    expect(tournamentServiceSpy.update).not.toHaveBeenCalled();
  });
});
