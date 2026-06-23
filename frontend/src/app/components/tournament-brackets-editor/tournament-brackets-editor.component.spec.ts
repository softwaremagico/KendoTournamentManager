import {of, Subject} from 'rxjs';
import {CdkDragDrop} from '@angular/cdk/drag-drop';
import {TeamService} from '../../services/team.service';
import {GroupService} from '../../services/group.service';
import {GroupLinkService} from '../../services/group-link.service';
import {RbacService} from '../../services/rbac/rbac.service';
import {SystemOverloadService} from '../../services/notifications/system-overload.service';
import {GroupsUpdatedService} from './tournament-brackets/groups-updated.service';
import {NumberOfWinnersUpdatedService} from '../../services/notifications/number-of-winners-updated.service';
import {RxStompService} from '../../websockets/rx-stomp.service';
import {EnvironmentService} from '../../environment.service';
import {TournamentChangedService} from './tournament-brackets/tournament-changed.service';
import {CsvService} from '../../services/csv-service';
import {MessageService} from '../../services/message.service';
import {TranslocoService} from '@ngneat/transloco';
import {Group} from '../../models/group';
import {Team} from '../../models/team';
import {GroupLink} from '../../models/group-link.model';
import {RbacActivity} from '../../services/rbac/rbac.activity';
import {TournamentBracketsEditorComponent} from './tournament-brackets-editor.component';

describe('TournamentBracketsEditorComponent', () => {
  let component: TournamentBracketsEditorComponent;
  let teamServiceSpy: jasmine.SpyObj<TeamService>;
  let groupServiceSpy: jasmine.SpyObj<GroupService>;
  let groupLinkServiceSpy: jasmine.SpyObj<GroupLinkService>;
  let rbacServiceSpy: jasmine.SpyObj<RbacService>;
  let systemOverloadServiceMock: SystemOverloadService;
  let groupsUpdatedServiceMock: GroupsUpdatedService;
  let numberOfWinnersUpdatedServiceMock: NumberOfWinnersUpdatedService;
  let rxStompServiceSpy: jasmine.SpyObj<RxStompService>;
  let environmentServiceSpy: jasmine.SpyObj<EnvironmentService>;
  let tournamentChangedServiceMock: TournamentChangedService;
  let csvServiceSpy: jasmine.SpyObj<CsvService>;
  let messageServiceSpy: jasmine.SpyObj<MessageService>;
  let translocoServiceSpy: jasmine.SpyObj<TranslocoService>;

  const createTeam = (id: number, name: string): Team => ({
    id,
    name,
    members: []
  } as unknown as Team);

  const createGroup = (id: number, level: number, index: number, teams: Team[] = []): Group => ({
    id,
    level,
    index,
    teams,
    fights: []
  } as unknown as Group);

  beforeEach(() => {
    teamServiceSpy = jasmine.createSpyObj('TeamService', ['getFromTournament']);
    groupServiceSpy = jasmine.createSpyObj('GroupService', [
      'getFromTournament',
      'deleteTeamsFromTournament',
      'addGroup',
      'deleteGroup',
      'addTeamsToGroup',
      'deleteAllTeamsFromTournament'
    ]);
    groupLinkServiceSpy = jasmine.createSpyObj('GroupLinkService', ['getFromTournament']);
    rbacServiceSpy = jasmine.createSpyObj('RbacService', ['isAllowed']);
    systemOverloadServiceMock = {
      isBusy: new Subject<boolean>()
    } as SystemOverloadService;
    groupsUpdatedServiceMock = {
      areTeamListUpdated: new Subject(),
      areGroupsUpdated: new Subject(),
      areTotalTeamsNumberUpdated: new Subject(),
      areRelationsUpdated: new Subject()
    } as GroupsUpdatedService;
    numberOfWinnersUpdatedServiceMock = {
      numberOfWinners: new Subject<number>()
    } as NumberOfWinnersUpdatedService;
    rxStompServiceSpy = jasmine.createSpyObj('RxStompService', ['watch']);
    environmentServiceSpy = jasmine.createSpyObj('EnvironmentService', ['getWebsocketPrefix']);
    tournamentChangedServiceMock = {
      isTournamentChanged: new Subject()
    } as TournamentChangedService;
    csvServiceSpy = jasmine.createSpyObj('CsvService', ['addGroupLinks']);
    messageServiceSpy = jasmine.createSpyObj('MessageService', ['infoMessage', 'errorMessage']);
    translocoServiceSpy = jasmine.createSpyObj('TranslocoService', ['translate']);

    environmentServiceSpy.getWebsocketPrefix.and.returnValue('/ws');
    rxStompServiceSpy.watch.and.returnValue(of({ body: '{}' } as any));

    component = new TournamentBracketsEditorComponent(
      teamServiceSpy,
      groupServiceSpy,
      groupLinkServiceSpy,
      rbacServiceSpy,
      systemOverloadServiceMock,
      groupsUpdatedServiceMock,
      numberOfWinnersUpdatedServiceMock,
      rxStompServiceSpy,
      environmentServiceSpy,
      tournamentChangedServiceMock,
      csvServiceSpy,
      messageServiceSpy,
      translocoServiceSpy
    );

    component.tournament = { id: 1, name: 'Tournament 1' } as any;
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should convert group relations grouped by source level', () => {
    const relations = [
      {
        source: { level: 0, index: 1 },
        destination: { index: 2 },
        winner: 0
      },
      {
        source: { level: 0, index: 3 },
        destination: { index: 4 },
        winner: 1
      }
    ] as unknown as GroupLink[];

    const converted = component.convert(relations);

    expect(converted.get(0)?.length).toBe(2);
    expect(converted.get(0)?.[0]).toEqual({ src: 1, dest: 2, winner: 0 });
    expect(converted.get(0)?.[1]).toEqual({ src: 3, dest: 4, winner: 1 });
  });

  it('should return empty relations map when convert receives undefined', () => {
    const converted = component.convert(undefined as unknown as GroupLink[]);

    expect(converted.size).toBe(0);
  });

  it('should select group and emit when RBAC allows', () => {
    rbacServiceSpy.isAllowed.and.returnValue(true);
    const group = createGroup(10, 0, 0);
    spyOn(component.editorSelectedGroup, 'emit');

    component.selectGroup(group);

    expect(rbacServiceSpy.isAllowed).toHaveBeenCalledOnceWith(RbacActivity.SELECT_GROUP);
    expect(component.selectedGroup).toBe(group);
    expect(component.editorSelectedGroup.emit).toHaveBeenCalledOnceWith(group);
  });

  it('should not select group when RBAC does not allow', () => {
    rbacServiceSpy.isAllowed.and.returnValue(false);
    const group = createGroup(10, 0, 0);
    spyOn(component.editorSelectedGroup, 'emit');

    component.selectGroup(group);

    expect(component.selectedGroup).toBeUndefined();
    expect(component.editorSelectedGroup.emit).not.toHaveBeenCalled();
  });

  it('should remove team and call deleteTeamsFromTournament', () => {
    const beta = createTeam(2, 'Beta');
    const alpha = createTeam(1, 'Alpha');

    component.teamListData.teams = [beta, alpha];
    component.teamListData.filteredTeams = [beta, alpha];
    groupServiceSpy.deleteTeamsFromTournament.and.returnValue(of({} as any));

    const event = {
      previousContainer: { data: [alpha] },
      container: { data: [] },
      previousIndex: 0,
      currentIndex: 0
    } as unknown as CdkDragDrop<Team[], any>;

    component.removeTeam(event);

    expect(groupServiceSpy.deleteTeamsFromTournament).toHaveBeenCalledOnceWith(1, component.teamListData.teams);
    expect(component.teamListData.teams.map(t => t.name)).toEqual(['Alpha', 'Beta']);
    expect(component.teamListData.filteredTeams.map(t => t.name)).toEqual(['Alpha', 'Beta']);
  });

  it('should add a level 0 group with index equal to current level 0 count', () => {
    component.groups = [createGroup(1, 0, 0), createGroup(2, 1, 0), createGroup(3, 0, 1)];
    groupServiceSpy.addGroup.and.returnValue(of(createGroup(4, 0, 2)));
    spyOn(component, 'updateData');

    component.addGroup();

    const sentGroup = groupServiceSpy.addGroup.calls.mostRecent().args[0] as Group;
    expect(sentGroup.level).toBe(0);
    expect(sentGroup.index).toBe(2);
    expect(sentGroup.tournament).toBe(component.tournament);
    expect(component.updateData).toHaveBeenCalledOnceWith(true, false);
  });

  it('should delete provided group and refresh data', () => {
    const group = createGroup(5, 0, 0);
    groupServiceSpy.deleteGroup.and.returnValue(of(undefined));
    spyOn(component, 'updateData');

    component.deleteGroup(group);

    expect(groupServiceSpy.deleteGroup).toHaveBeenCalledOnceWith(group);
    expect(component.updateData).toHaveBeenCalledOnceWith(true, false);
  });

  it('should update groups teams by sending one request per group', () => {
    const g1 = createGroup(1, 0, 0, [createTeam(1, 'A')]);
    const g2 = createGroup(2, 0, 1, [createTeam(2, 'B')]);
    groupServiceSpy.addTeamsToGroup.and.returnValue(of(createGroup(99, 0, 0)));
    spyOn(component, 'updateData');

    component.updateGroupsTeams([g1, g2]);

    expect(groupServiceSpy.addTeamsToGroup).toHaveBeenCalledTimes(2);
    expect(groupServiceSpy.addTeamsToGroup).toHaveBeenCalledWith(1, g1.teams);
    expect(groupServiceSpy.addTeamsToGroup).toHaveBeenCalledWith(2, g2.teams);
    expect(component.updateData).toHaveBeenCalledOnceWith(true, false);
  });

  it('should remove all teams and notify team list update', () => {
    groupServiceSpy.deleteAllTeamsFromTournament.and.returnValue(of([]));
    const nextSpy = spyOn(groupsUpdatedServiceMock.areTeamListUpdated, 'next');

    component.removeAllTeams();

    expect(groupServiceSpy.deleteAllTeamsFromTournament).toHaveBeenCalledOnceWith(1);
    expect(nextSpy).toHaveBeenCalledOnceWith([]);
  });

  it('should call updateData from handleFileInput after importing links', () => {
    const file = new File(['a,b'], 'links.csv', { type: 'text/csv' });
    const input = document.createElement('input');
    const fileList = {
      length: 1,
      item: (_index: number) => file
    } as unknown as FileList;

    Object.defineProperty(input, 'files', {
      configurable: true,
      get: () => fileList
    });

    csvServiceSpy.addGroupLinks.and.returnValue(of([]));
    spyOn(component, 'updateData');

    component.handleFileInput({ currentTarget: input } as unknown as Event);

    expect(csvServiceSpy.addGroupLinks).toHaveBeenCalledOnceWith(file, 1);
    expect(messageServiceSpy.infoMessage).toHaveBeenCalledOnceWith('groupLinkStored');
    expect(component.updateData).toHaveBeenCalledOnceWith(true, false);
  });
});


