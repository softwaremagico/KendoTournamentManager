import {of} from 'rxjs';
import {CdkDragDrop} from '@angular/cdk/drag-drop';
import {TournamentRolesComponent} from './tournament-roles.component';
import {ParticipantService} from '../../../services/participant.service';
import {RoleService} from '../../../services/role.service';
import {MessageService} from '../../../services/message.service';
import {RbacService} from '../../../services/rbac/rbac.service';
import {FilterResetService} from '../../../services/notifications/filter-reset.service';
import {StatisticsChangedService} from '../../../services/notifications/statistics-changed.service';
import {TeamService} from '../../../services/team.service';
import {Participant} from '../../../models/participant';
import {Role} from '../../../models/role';
import {RoleType} from '../../../models/role-type';
import {Team} from '../../../models/team';

describe('TournamentRolesComponent', () => {
  let component: TournamentRolesComponent;
  let participantServiceSpy: jasmine.SpyObj<ParticipantService>;
  let roleServiceSpy: jasmine.SpyObj<RoleService>;
  let messageServiceSpy: jasmine.SpyObj<MessageService>;
  let rbacServiceSpy: jasmine.SpyObj<RbacService>;
  let filterResetServiceMock: FilterResetService;
  let statisticsChangedServiceMock: StatisticsChangedService;
  let teamServiceSpy: jasmine.SpyObj<TeamService>;

  const createParticipant = (
    id: number,
    name: string,
    lastname: string,
    hasAvatar: boolean = false
  ): Participant => ({
    id,
    name,
    lastname,
    idCard: `ID-${id}`,
    hasAvatar,
    locked: false,
    club: { id: 1, name: 'Club 1' } as any
  } as unknown as Participant);

  const createRole = (participant: Participant, roleType: RoleType): Role => ({
    id: participant.id,
    participant,
    roleType
  } as unknown as Role);

  beforeEach(() => {
    participantServiceSpy = jasmine.createSpyObj('ParticipantService', ['getAll']);
    roleServiceSpy = jasmine.createSpyObj('RoleService', [
      'getFromTournamentAndTypes',
      'add',
      'deleteByParticipantAndTournament',
      'getRolesByTournament'
    ]);
    messageServiceSpy = jasmine.createSpyObj('MessageService', ['infoMessage']);
    rbacServiceSpy = jasmine.createSpyObj('RbacService', ['isAllowed']);
    filterResetServiceMock = {
      resetFilter: {
        next: jasmine.createSpy('next')
      }
    } as unknown as FilterResetService;
    statisticsChangedServiceMock = {
      areStatisticsChanged: {
        next: jasmine.createSpy('next')
      }
    } as unknown as StatisticsChangedService;
    teamServiceSpy = jasmine.createSpyObj('TeamService', ['getFromTournament']);

    component = new TournamentRolesComponent(
      participantServiceSpy,
      roleServiceSpy,
      messageServiceSpy,
      rbacServiceSpy,
      filterResetServiceMock,
      statisticsChangedServiceMock,
      teamServiceSpy
    );

    component.tournament = {
      id: 10,
      name: 'Tournament',
      locked: false
    } as any;
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize empty container for role when it does not exist', () => {
    const container = component.getParticipantsContainer(RoleType.REFEREE);

    expect(container).toEqual([]);
    expect(component.participants.get(RoleType.REFEREE)).toBe(container);
  });

  it('should load participants and roles on ngOnInit, set avatars flag and lock competitors in teams', () => {
    const competitorWithRole = createParticipant(1, 'John', 'Competitor');
    const participantA = createParticipant(2, 'Ana', 'Alpha');
    const participantB = createParticipant(3, 'Zoe', 'Zulu', true);

    participantServiceSpy.getAll.and.returnValue(of([
      competitorWithRole,
      participantA,
      participantB
    ]));
    roleServiceSpy.getFromTournamentAndTypes.and.returnValue(
      of([createRole({ ...competitorWithRole, club: undefined } as any, RoleType.COMPETITOR)])
    );
    teamServiceSpy.getFromTournament.and.returnValue(of([
      { members: [competitorWithRole] } as unknown as Team
    ]));

    component.ngOnInit();

    expect(component.participants.get(RoleType.COMPETITOR)?.length).toBe(1);
    expect(component.participants.get(RoleType.COMPETITOR)?.[0].club?.name).toBe('Club 1');
    expect(component.participants.get(RoleType.COMPETITOR)?.[0].locked).toBeTrue();
    expect(component.userListData.participants.map(p => p.lastname)).toEqual(['Alpha', 'Zulu']);
    expect(component.showAvatars).toBeTrue();
  });

  it('should return undefined in transferCard when source and destination are equal', () => {
    const list = [createParticipant(1, 'A', 'A')];
    const sameContainer = { data: list };
    const event = {
      previousContainer: sameContainer,
      container: sameContainer,
      previousIndex: 0,
      currentIndex: 0
    } as unknown as CdkDragDrop<Participant[], any>;

    expect(component.transferCard(event)).toBeUndefined();
  });

  it('should add role on dropParticipant and remove participant from user lists', () => {
    const participant = createParticipant(1, 'John', 'Doe');
    component.userListData.participants = [participant];
    component.userListData.filteredParticipants = [participant];
    component.participants.set(RoleType.REFEREE, []);
    roleServiceSpy.add.and.returnValue(of({} as Role));

    const event = {
      previousContainer: { data: [participant] },
      container: { data: component.getParticipantsContainer(RoleType.REFEREE) },
      previousIndex: 0,
      currentIndex: 0
    } as unknown as CdkDragDrop<Participant[], any>;

    component.dropParticipant(event, RoleType.REFEREE);

    expect(roleServiceSpy.add).toHaveBeenCalled();
    expect(messageServiceSpy.infoMessage).toHaveBeenCalledWith('infoRoleStored');
    expect((filterResetServiceMock.resetFilter.next as jasmine.Spy)).toHaveBeenCalledWith(true);
    expect((statisticsChangedServiceMock.areStatisticsChanged.next as jasmine.Spy)).toHaveBeenCalledWith(true);
    expect(component.userListData.participants).toEqual([]);
    expect(component.userListData.filteredParticipants).toEqual([]);
  });

  it('should remove role and move participant back to user lists', () => {
    const participant = createParticipant(1, 'John', 'Doe');
    component.userListData.participants = [];
    component.userListData.filteredParticipants = [];
    roleServiceSpy.deleteByParticipantAndTournament.and.returnValue(of({} as Role));

    const event = {
      previousContainer: { data: [participant] },
      container: { data: [] },
      previousIndex: 0,
      currentIndex: 0
    } as unknown as CdkDragDrop<Participant[], any>;

    component.removeRole(event);

    expect(roleServiceSpy.deleteByParticipantAndTournament).toHaveBeenCalledWith(participant, component.tournament);
    expect(messageServiceSpy.infoMessage).toHaveBeenCalledWith('infoRoleDeleted');
    expect((statisticsChangedServiceMock.areStatisticsChanged.next as jasmine.Spy)).toHaveBeenCalledWith(true);
    expect(component.userListData.participants).toContain(participant);
    expect(component.userListData.filteredParticipants).toContain(participant);
  });

  it('should download roles pdf and disable loading flag at the end', () => {
    const blob = new Blob(['pdf-content'], { type: 'application/pdf' });
    const anchorMock = {
      href: '',
      download: '',
      click: jasmine.createSpy('click')
    } as unknown as HTMLAnchorElement;

    roleServiceSpy.getRolesByTournament.and.returnValue(of(blob));
    spyOn(window.URL, 'createObjectURL').and.returnValue('blob:mock-url');
    spyOn(document, 'createElement').and.returnValue(anchorMock as any);

    component.downloadPDF();

    expect(roleServiceSpy.getRolesByTournament).toHaveBeenCalledOnceWith(10);
    expect(window.URL.createObjectURL).toHaveBeenCalled();
    expect(anchorMock.download).toBe('Role List - Tournament.pdf');
    expect((anchorMock.click as jasmine.Spy)).toHaveBeenCalled();
    expect(component.loadingGlobal).toBeFalse();
  });

  it('should count role participants and return 0 for missing role', () => {
    component.participants.set(RoleType.ORGANIZER, [createParticipant(1, 'A', 'A')]);

    expect(component.countRole(RoleType.ORGANIZER)).toBe(1);
    expect(component.countRole(RoleType.PRESS)).toBe(0);
  });
});

