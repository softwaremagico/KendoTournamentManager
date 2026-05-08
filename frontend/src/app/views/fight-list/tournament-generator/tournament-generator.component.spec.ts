import { BehaviorSubject, of } from 'rxjs';
import { TournamentGeneratorComponent } from './tournament-generator.component';
import { Router } from '@angular/router';
import { RbacService } from '../../../services/rbac/rbac.service';
import { TournamentService } from '../../../services/tournament.service';
import { FightService } from '../../../services/fight.service';
import { MessageService } from '../../../services/message.service';
import { GroupService } from '../../../services/group.service';
import { TournamentChangedService } from '../../../components/tournament-brackets-editor/tournament-brackets/tournament-changed.service';
import { TournamentExtendedPropertiesService } from '../../../services/tournament-extended-properties.service';
import { NumberOfWinnersUpdatedService } from '../../../services/notifications/number-of-winners-updated.service';
import { Tournament } from '../../../models/tournament';
import { TournamentScore } from '../../../models/tournament-score.model';
import { TournamentType } from '../../../models/tournament-type';
import { Group } from '../../../models/group';
import { TournamentExtraPropertyKey } from '../../../models/tournament-extra-property-key';

describe('TournamentGeneratorComponent', () => {
  let component: TournamentGeneratorComponent;
  let routerSpy: jasmine.SpyObj<Router>;
  let rbacServiceSpy: jasmine.SpyObj<RbacService>;
  let tournamentServiceSpy: jasmine.SpyObj<TournamentService>;
  let fightServiceSpy: jasmine.SpyObj<FightService>;
  let messageServiceSpy: jasmine.SpyObj<MessageService>;
  let groupServiceSpy: jasmine.SpyObj<GroupService>;
  let tournamentChangedServiceMock: TournamentChangedService;
  let tournamentExtendedPropertiesServiceSpy: jasmine.SpyObj<TournamentExtendedPropertiesService>;
  let numberOfWinnersUpdatedServiceMock: NumberOfWinnersUpdatedService;

  const buildTournament = (type: TournamentType = TournamentType.CHAMPIONSHIP): Tournament => {
    const t = new Tournament();
    t.id = 5;
    t.name = 'Championship';
    t.type = type;
    t.tournamentScore = new TournamentScore();
    return t;
  };

  beforeEach(() => {
    routerSpy = jasmine.createSpyObj('Router', ['navigate', 'getCurrentNavigation']);
    rbacServiceSpy = jasmine.createSpyObj('RbacService', ['isAllowed']);
    tournamentServiceSpy = jasmine.createSpyObj('TournamentService', ['get', 'setNumberOfWinners']);
    fightServiceSpy = jasmine.createSpyObj('FightService', ['create']);
    messageServiceSpy = jasmine.createSpyObj('MessageService', ['infoMessage', 'warningMessage', 'errorMessage']);
    groupServiceSpy = jasmine.createSpyObj('GroupService', ['getGroupsByTournament', 'refreshNonStartedGroups']);
    tournamentExtendedPropertiesServiceSpy = jasmine.createSpyObj('TournamentExtendedPropertiesService', ['getByTournament']);

    tournamentChangedServiceMock = {
      isTournamentChanged: new BehaviorSubject<any>(null)
    } as TournamentChangedService;

    numberOfWinnersUpdatedServiceMock = {
      numberOfWinners: new BehaviorSubject<number>(1)
    } as NumberOfWinnersUpdatedService;

    rbacServiceSpy.isAllowed.and.returnValue(true);
    routerSpy.getCurrentNavigation.and.returnValue({
      extras: { state: { tournamentId: 5, editionDisabled: false } }
    } as any);
    tournamentServiceSpy.get.and.returnValue(of(buildTournament(TournamentType.CHAMPIONSHIP)));
    tournamentExtendedPropertiesServiceSpy.getByTournament.and.returnValue(of([
      { propertyKey: TournamentExtraPropertyKey.NUMBER_OF_WINNERS, propertyValue: '2' } as any
    ]));
    fightServiceSpy.create.and.returnValue(of([] as any));
    groupServiceSpy.getGroupsByTournament.and.returnValue(of(new Blob(['pdf'])));
    groupServiceSpy.refreshNonStartedGroups.and.returnValue(of({}) as any);
    tournamentServiceSpy.setNumberOfWinners.and.returnValue(of({}) as any);

    component = new TournamentGeneratorComponent(
      routerSpy,
      rbacServiceSpy,
      tournamentServiceSpy,
      fightServiceSpy,
      messageServiceSpy,
      groupServiceSpy,
      tournamentChangedServiceMock,
      tournamentExtendedPropertiesServiceSpy,
      numberOfWinnersUpdatedServiceMock
    );

    (component as any).tournamentBracketsEditorComponent = {
      addGroup: jasmine.createSpy('addGroup'),
      deleteLast: jasmine.createSpy('deleteLast'),
      updateData: jasmine.createSpy('updateData')
    } as any;
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should read tournamentId and editionDisabled from router state', () => {
    expect(component.tournamentId).toBe(5);
    expect(component.groupsDisabled).toBeFalse();
  });

  it('should navigate back when state is missing', () => {
    routerSpy.getCurrentNavigation.and.returnValue(null);

    component = new TournamentGeneratorComponent(
      routerSpy,
      rbacServiceSpy,
      tournamentServiceSpy,
      fightServiceSpy,
      messageServiceSpy,
      groupServiceSpy,
      tournamentChangedServiceMock,
      tournamentExtendedPropertiesServiceSpy,
      numberOfWinnersUpdatedServiceMock
    );

    expect(routerSpy.navigate).toHaveBeenCalledWith(['/tournaments/fights'], { state: { tournamentId: undefined } });
  });

  it('should load tournament and refresh winners on ngOnInit', () => {
    component.ngOnInit();

    expect(tournamentServiceSpy.get).toHaveBeenCalledWith(5);
    expect(component.tournament).toBeTruthy();
    expect(component.numberOfWinners).toBe(2);
  });

  it('should emit tournament changed on ngOnInit', () => {
    component.ngOnInit();

    expect(tournamentChangedServiceMock.isTournamentChanged.value).toEqual(component.tournament);
  });

  it('should add group when conditions are met', () => {
    component.groupsLevelZero = [({ level: 0 } as Group)];
    component.totalTeams = 6;
    (component as any).updatingGroup = false;

    component.addGroup();

    expect((component as any).tournamentBracketsEditorComponent.addGroup).toHaveBeenCalled();
    expect((component as any).updatingGroup).toBeTrue();
  });

  it('should not add group when limit reached', () => {
    component.groupsLevelZero = [({ level: 0 } as Group), ({ level: 0 } as Group), ({ level: 0 } as Group)];
    component.totalTeams = 6;

    component.addGroup();

    expect((component as any).tournamentBracketsEditorComponent.addGroup).not.toHaveBeenCalled();
  });

  it('should delete group when not updating', () => {
    (component as any).updatingGroup = false;

    component.deleteGroup();

    expect((component as any).tournamentBracketsEditorComponent.deleteLast).toHaveBeenCalled();
    expect((component as any).updatingGroup).toBeTrue();
  });

  it('should update groups and groupsLevelZero on groupsUpdated', () => {
    const groups = [
      { id: 1, level: 0 } as Group,
      { id: 2, level: 1 } as Group,
      { id: 3, level: 0 } as Group
    ];

    component.groupsUpdated(groups);

    expect(component.groups.length).toBe(3);
    expect(component.groupsLevelZero.length).toBe(2);
  });

  it('should update totalTeams on teamsSizeUpdated', () => {
    component.teamsSizeUpdated(12);

    expect(component.totalTeams).toBe(12);
  });

  it('should reset updatingGroup on groupsActionsDisabled', () => {
    (component as any).updatingGroup = true;

    component.groupsActionsDisabled(true);

    expect((component as any).updatingGroup).toBeFalse();
  });

  it('should create fights and navigate back on generateElements', () => {
    component.generateElements();

    expect(fightServiceSpy.create).toHaveBeenCalledWith(5, 0);
    expect(messageServiceSpy.infoMessage).toHaveBeenCalledWith('infoFightCreated');
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/tournaments/fights'], { state: { tournamentId: 5 } });
    expect(component.loadingGlobal).toBeFalse();
  });

  it('should change number of winners and notify', () => {
    component.tournament = buildTournament();

    component.changeNumberOfWinners(4);

    expect(tournamentServiceSpy.setNumberOfWinners).toHaveBeenCalledWith(component.tournament, 4);
    expect(numberOfWinnersUpdatedServiceMock.numberOfWinners.value).toBe(4);
    expect(messageServiceSpy.infoMessage).toHaveBeenCalledWith('infoTournamentUpdated');
  });

  it('should refresh groups and call editor updateData', () => {
    component.refreshGroups();

    expect(groupServiceSpy.refreshNonStartedGroups).toHaveBeenCalledWith(5, 1);
    expect((component as any).tournamentBracketsEditorComponent.updateData).toHaveBeenCalledWith(true, true);
    expect(component.loadingGlobal).toBeFalse();
  });

  it('should download pdf when tournament exists', () => {
    component.tournament = buildTournament();
    spyOn(URL, 'createObjectURL').and.returnValue('blob:url');
    const anchor = { click: jasmine.createSpy('click') } as any;
    spyOn(document, 'createElement').and.returnValue(anchor);

    component.downloadPDF();

    expect(groupServiceSpy.getGroupsByTournament).toHaveBeenCalledWith(5);
    expect(anchor.click).toHaveBeenCalled();
  });
});


