import { of } from 'rxjs';
import { TournamentCustomScoresFormComponent } from './tournament-custom-scores-form.component';
import { RbacService } from '../../services/rbac/rbac.service';
import { TranslocoService } from '@ngneat/transloco';
import { Tournament } from '../../models/tournament';
import { TournamentScore } from '../../models/tournament-score.model';

describe('TournamentCustomScoresFormComponent', () => {
  let component: TournamentCustomScoresFormComponent;
  let rbacServiceSpy: jasmine.SpyObj<RbacService>;
  let translocoServiceSpy: jasmine.SpyObj<TranslocoService>;

  const buildTournament = (): Tournament => {
    const tournament = new Tournament();
    tournament.id = 1;
    tournament.name = 'Test Tournament';
    tournament.tournamentScore = new TournamentScore();
    return tournament;
  };

  beforeEach(() => {
    rbacServiceSpy = jasmine.createSpyObj('RbacService', ['isAllowed']);
    translocoServiceSpy = jasmine.createSpyObj('TranslocoService', ['translate', 'selectTranslate']);

    rbacServiceSpy.isAllowed.and.returnValue(true);
    translocoServiceSpy.translate.and.returnValue('translated');
    translocoServiceSpy.selectTranslate.and.returnValue(of('translated'));

    component = new TournamentCustomScoresFormComponent(rbacServiceSpy, translocoServiceSpy);
    component.tournament = buildTournament();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should keep tournament input value', () => {
    const tournament = buildTournament();
    component.tournament = tournament;

    expect(component.tournament).toBe(tournament);
  });

  it('should initialize output emitter', () => {
    expect(component.onClosed).toBeTruthy();
  });

  it('should emit onClosed when requested', () => {
    spyOn(component.onClosed, 'emit');

    component.onClosed.emit();

    expect(component.onClosed.emit).toHaveBeenCalled();
  });

  it('should execute ngOnInit without errors', () => {
    expect(() => component.ngOnInit()).not.toThrow();
  });

  it('should expose TournamentFormValidationFields reference', () => {
    expect((component as any).TournamentFormValidationFields).toBeTruthy();
  });

  it('should expose Type reference', () => {
    expect((component as any).Type).toBeTruthy();
  });
});

