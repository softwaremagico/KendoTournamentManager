import {Subject} from 'rxjs';
import {CdkDrag, CdkDropList} from '@angular/cdk/drag-drop';
import {Duel} from '../../../models/duel';
import {Fight} from '../../../models/fight';
import {Participant} from '../../../models/participant';
import {DuelChangedService} from '../../../services/notifications/duel-changed.service';
import {MembersOrderChangedService} from '../../../services/notifications/members-order-changed.service';
import {DuelComponent} from './duel.component';

describe('DuelComponent', () => {
  let component: DuelComponent;
  let duelChangedServiceMock: DuelChangedService;
  let membersOrderChangedServiceMock: MembersOrderChangedService;
  let duelChangedSubject: Subject<Duel>;

  const createParticipant = (id: number, name: string): Participant => ({
    id,
    name,
    lastname: 'Test',
    idCard: `ID${id}`,
    hasAvatar: false,
    locked: false
  } as unknown as Participant);

  const createDuel = (id: number = 1): Duel => {
    const duel = new Duel();
    duel.id = id;
    duel.competitor1 = undefined;
    duel.competitor2 = undefined;
    duel.competitor1Score = [];
    duel.competitor2Score = [];
    duel.competitor1ScoreTime = [];
    duel.competitor2ScoreTime = [];
    return duel;
  };

  const createTeam = (members: Participant[]) => ({
    name: 'Team',
    members
  });

  const createFight = (duels: Duel[], team1Members: Participant[], team2Members: Participant[]): Fight => ({
    id: 1,
    duels,
    team1: createTeam(team1Members),
    team2: createTeam(team2Members)
  } as unknown as Fight);

  beforeEach(() => {
    duelChangedSubject = new Subject<Duel>();
    duelChangedServiceMock = {
      isDuelUpdated: duelChangedSubject
    } as DuelChangedService;

    membersOrderChangedServiceMock = {
      membersOrderChanged: new Subject()
    } as MembersOrderChangedService;

    component = new DuelComponent(duelChangedServiceMock, membersOrderChangedServiceMock);
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize inputs with default values', () => {
    expect(component.showAvatars).toBeFalse();
    expect(component.over).toBeFalse();
    expect(component.duelSelected).toBeUndefined();
  });

  it('should subscribe to duelChangedService on init and update duelSelected', () => {
    const selectedDuel = createDuel(5);

    component.ngOnInit();
    duelChangedSubject.next(selectedDuel);

    expect(component.duelSelected).toBe(selectedDuel);
  });

  it('should allow drop when participant is in left team', () => {
    const p1 = createParticipant(1, 'A');
    const p2 = createParticipant(2, 'B');
    const duel1 = createDuel();
    const duel2 = createDuel();
    const fight = createFight([duel1, duel2], [p1, p2], []);

    component.fight = fight;
    const predicate = component.dropListEnterPredicate(fight, true);

    const dragItem = { data: p1 } as CdkDrag<Participant>;
    const dropList = {} as CdkDropList;

    expect(predicate(dragItem, dropList)).toBeTrue();
  });

  it('should not allow drop when participant is not in left team', () => {
    const p1 = createParticipant(1, 'A');
    const p2 = createParticipant(2, 'B');
    const duel1 = createDuel();
    const duel2 = createDuel();
    const fight = createFight([duel1, duel2], [p1], [p2]);

    component.fight = fight;
    const predicate = component.dropListEnterPredicate(fight, true);

    const dragItem = { data: p2 } as CdkDrag<Participant>;
    const dropList = {} as CdkDropList;

    expect(predicate(dragItem, dropList)).toBeFalse();
  });

  it('should allow drop when participant is in right team', () => {
    const p1 = createParticipant(1, 'A');
    const p2 = createParticipant(2, 'B');
    const duel1 = createDuel();
    const duel2 = createDuel();
    const fight = createFight([duel1, duel2], [p1], [p2]);

    component.fight = fight;
    const predicate = component.dropListEnterPredicate(fight, false);

    const dragItem = { data: p2 } as CdkDrag<Participant>;
    const dropList = {} as CdkDropList;

    expect(predicate(dragItem, dropList)).toBeTrue();
  });

  it('should not allow drop when participant is not in right team', () => {
    const p1 = createParticipant(1, 'A');
    const p2 = createParticipant(2, 'B');
    const duel1 = createDuel();
    const duel2 = createDuel();
    const fight = createFight([duel1, duel2], [p1], [p2]);

    component.fight = fight;
    const predicate = component.dropListEnterPredicate(fight, false);

    const dragItem = { data: p1 } as CdkDrag<Participant>;
    const dropList = {} as CdkDropList;

    expect(predicate(dragItem, dropList)).toBeFalse();
  });

  it('should swap left competitors using swapMembers', () => {
    const p1 = createParticipant(1, 'A');
    const p2 = createParticipant(2, 'B');

    const duel1 = createDuel();
    duel1.competitor1 = p1;
    const duel2 = createDuel();
    duel2.competitor1 = p2;
    const fight = createFight([duel1, duel2], [p1, p2], []);

    component.fight = fight;
    spyOn((membersOrderChangedServiceMock.membersOrderChanged as Subject<Fight>), 'next');

    component.swapMembers(0, 1, true);

    expect(component.fight.duels[0].competitor1).toEqual(p2);
    expect(component.fight.duels[1].competitor1).toEqual(p1);
    expect((membersOrderChangedServiceMock.membersOrderChanged as Subject<Fight>).next).toHaveBeenCalledOnceWith(fight);
  });

  it('should swap right competitors using swapMembers', () => {
    const p1 = createParticipant(1, 'A');
    const p2 = createParticipant(2, 'B');

    const duel1 = createDuel();
    duel1.competitor2 = p1;
    const duel2 = createDuel();
    duel2.competitor2 = p2;
    const fight = createFight([duel1, duel2], [], [p1, p2]);

    component.fight = fight;
    spyOn((membersOrderChangedServiceMock.membersOrderChanged as Subject<Fight>), 'next');

    component.swapMembers(0, 1, false);

    expect(component.fight.duels[0].competitor2).toEqual(p2);
    expect(component.fight.duels[1].competitor2).toEqual(p1);
  });

  it('should stop updating duelSelected after ngOnDestroy', () => {
    const duel1 = createDuel(1);
    const duel2 = createDuel(2);

    component.ngOnInit();
    duelChangedSubject.next(duel1);
    component.ngOnDestroy();
    duelChangedSubject.next(duel2);

    expect(component.duelSelected).toBe(duel1);
  });
});




