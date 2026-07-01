import {of} from 'rxjs';
import {TournamentFormPopupComponent} from './tournament-form-popup.component';
import {UserSessionService} from '../../../services/user-session.service';
import {TranslocoService} from '@jsverse/transloco';
import {Tournament} from '../../../models/tournament';
import {AuthenticatedUser} from '../../../models/authenticated-user';
import {TournamentScore} from '../../../models/tournament-score.model';

describe('TournamentFormPopupComponent', () => {
  let component: TournamentFormPopupComponent;
  let sessionServiceSpy: jasmine.SpyObj<UserSessionService>;
  let translocoServiceSpy: jasmine.SpyObj<TranslocoService>;

  const testUser: AuthenticatedUser = {
    id: 1,
    username: 'admin',
    name: 'Admin',
    lastname: 'User'
  } as AuthenticatedUser;

  const buildTournament = (): Tournament => {
    const t = new Tournament();
    t.id = 10;
    t.name = 'Test Tournament';
    t.tournamentScore = new TournamentScore();
    return t;
  };

  beforeEach(() => {
    sessionServiceSpy = jasmine.createSpyObj('UserSessionService', ['getUser']);
    translocoServiceSpy = jasmine.createSpyObj('TranslocoService', ['selectTranslate', 'translate']);

    sessionServiceSpy.getUser.and.returnValue(testUser);
    translocoServiceSpy.selectTranslate.and.returnValue(of('translated'));

    component = new TournamentFormPopupComponent(sessionServiceSpy, translocoServiceSpy);
    component.tournament = buildTournament();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize EventEmitters on creation', () => {
    expect(component.closed).toBeTruthy();
    expect(component.saved).toBeTruthy();
    expect(component.errorEvent).toBeTruthy();
  });

  it('should load the logged user in ngOnInit', () => {
    component.ngOnInit();

    expect(sessionServiceSpy.getUser).toHaveBeenCalled();
    expect((component as any).loggedUser).toEqual(testUser);
  });

  it('should clone the tournament to originalTournament in ngOnInit', () => {
    component.ngOnInit();

    expect((component as any).originalTournament).toBeTruthy();
    expect((component as any).originalTournament.id).toBe(10);
    expect((component as any).originalTournament).not.toBe(component.tournament);
  });

  it('should preserve @Input() tournament property', () => {
    const tournament = buildTournament();
    component.tournament = tournament;

    expect(component.tournament).toEqual(tournament);
  });

  it('should initialize with empty errors map', () => {
    expect((component as any).errors.size).toBe(0);
  });
});

