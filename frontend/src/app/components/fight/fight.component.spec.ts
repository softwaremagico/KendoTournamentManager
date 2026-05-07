import { Subject } from 'rxjs';
import { Duel } from '../../models/duel';
import { Fight } from '../../models/fight';
import { DuelChangedService } from '../../services/notifications/duel-changed.service';
import { MembersOrderChangedService } from '../../services/notifications/members-order-changed.service';
import { RbacService } from '../../services/rbac/rbac.service';
import { RbacActivity } from '../../services/rbac/rbac.activity';
import { FightComponent } from './fight.component';

describe('FightComponent', () => {
  let component: FightComponent;
  let duelChangedServiceMock: DuelChangedService;
  let membersOrderChangedServiceMock: MembersOrderChangedService;
  let rbacServiceSpy: jasmine.SpyObj<RbacService>;
  let duelChangedSubject: Subject<Duel>;
  let reorderAllowedSubject: Subject<boolean>;

  const createDuel = (id: number = 1, substitute: boolean = false): Duel => {
    const duel = new Duel();
    duel.id = id;
    duel.substitute = substitute;
    duel.finished = false;
    duel.competitor1Score = [];
    duel.competitor2Score = [];
    duel.competitor1ScoreTime = [];
    duel.competitor2ScoreTime = [];
    return duel;
  };

  const createTeam = (members: any[] = []) => ({
    name: 'Team',
    members
  });

  const createFight = (duels: Duel[], teamSize: number = 1): Fight => ({
    id: 1,
    duels,
    team1: createTeam(),
    team2: createTeam(),
    tournament: {
      teamSize,
      name: 'Test Tournament'
    }
  } as unknown as Fight);

  beforeEach(() => {
    duelChangedSubject = new Subject<Duel>();
    reorderAllowedSubject = new Subject<boolean>();

    duelChangedServiceMock = {
      isDuelUpdated: duelChangedSubject
    } as DuelChangedService;

    membersOrderChangedServiceMock = {
      membersOrderAllowed: reorderAllowedSubject
    } as MembersOrderChangedService;

    rbacServiceSpy = jasmine.createSpyObj('RbacService', ['isAllowed']);

    component = new FightComponent(duelChangedServiceMock, rbacServiceSpy, membersOrderChangedServiceMock);
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize inputs with default values', () => {
    expect(component.showAvatars).toBeFalse();
    expect(component.onlyShow).toBeFalse();
    expect(component.reorderAllowed).toBeTrue();
  });

  it('should subscribe to membersOrderAllowed on init and update reorderAllowed', () => {
    component.ngOnInit();
    reorderAllowedSubject.next(false);

    expect(component.reorderAllowed).toBeFalse();

    reorderAllowedSubject.next(true);
    expect(component.reorderAllowed).toBeTrue();
  });

  it('should show team title when teamSize > 1', () => {
    component.fight = createFight([], 3);

    expect(component.showTeamTitle()).toBeTrue();
  });

  it('should not show team title when teamSize is 1', () => {
    component.fight = createFight([], 1);

    expect(component.showTeamTitle()).toBeFalse();
  });

  it('should show team title by default when tournament is undefined', () => {
    component.fight = { duels: [] } as unknown as Fight;

    expect(component.showTeamTitle()).toBeTrue();
  });

  it('should select duel and emit event when RBAC allows and duel is not substitute', () => {
    rbacServiceSpy.isAllowed.and.returnValue(true);
    const duel = createDuel(1, false);
    spyOn(component.onSelectedDuel, 'emit');

    component.selectDuel(duel);

    expect(component.selectedDuel).toBe(duel);
    expect(component.onSelectedDuel.emit).toHaveBeenCalledOnceWith(duel);
  });

  it('should not select duel when duel is substitute', () => {
    rbacServiceSpy.isAllowed.and.returnValue(true);
    const duel = createDuel(1, true);
    spyOn(component.onSelectedDuel, 'emit');

    component.selectDuel(duel);

    expect(component.selectedDuel).toBeUndefined();
    expect(component.onSelectedDuel.emit).not.toHaveBeenCalled();
  });

  it('should not select duel when RBAC does not allow', () => {
    rbacServiceSpy.isAllowed.and.returnValue(false);
    const duel = createDuel(1, false);
    spyOn(component.onSelectedDuel, 'emit');

    component.selectDuel(duel);

    expect(component.selectedDuel).toBeUndefined();
    expect(component.onSelectedDuel.emit).not.toHaveBeenCalled();
  });

  it('should return true from isOver when duel is substitute', () => {
    const duel = createDuel(1, true);

    expect(component.isOver(duel)).toBeTrue();
  });

  it('should return true from isOver when duel is finished and not locked', () => {
    const duel = createDuel(1, false);
    duel.finished = true;
    component.locked = false;

    expect(component.isOver(duel)).toBeTrue();
  });

  it('should return false from isOver when duel is finished but locked', () => {
    const duel = createDuel(1, false);
    duel.finished = true;
    component.locked = true;

    expect(component.isOver(duel)).toBeFalse();
  });

  it('should update selectedDuel when duelChangedService emits matching duel', () => {
    const duel1 = createDuel(1);
    const duel2 = createDuel(2);
    component.fight = createFight([duel1, duel2]);

    component.ngOnInit();
    duelChangedSubject.next(duel1);

    expect(component.selected).toBeTrue();
    expect(component.selectedDuel).toBe(duel1);
  });

  it('should clear selectedDuel when duelChangedService emits non-matching duel', () => {
    const duel1 = createDuel(1);
    const duel2 = createDuel(2);
    const duel3 = createDuel(3);
    component.fight = createFight([duel1, duel2]);

    component.ngOnInit();
    duelChangedSubject.next(duel3);

    expect(component.selected).toBeFalse();
    expect(component.selectedDuel).toBeUndefined();
  });

  it('should stop updating selectedDuel after ngOnDestroy', () => {
    const duel1 = createDuel(1);
    const duel2 = createDuel(2);
    component.fight = createFight([duel1, duel2]);

    component.ngOnInit();
    duelChangedSubject.next(duel1);
    component.ngOnDestroy();
    duelChangedSubject.next(duel2);

    expect(component.selectedDuel).toBe(duel1);
  });
});


