import {of} from 'rxjs';
import {Router} from '@angular/router';
import {DatePipe} from '@angular/common';
import {BiitSnackbarService, NotificationType} from '@biit-solutions/wizardry-theme/info';
import {TournamentListComponent} from './tournament-list.component';
import {UserSessionService} from '../../services/user-session.service';
import {TournamentService} from '../../services/tournament.service';
import {RankingService} from '../../services/ranking.service';
import {MessageService} from '../../services/message.service';
import {RbacService} from '../../services/rbac/rbac.service';
import {SystemOverloadService} from '../../services/notifications/system-overload.service';
import {AchievementsService} from '../../services/achievements.service';
import {TranslocoService} from '@ngneat/transloco';
import {TableColumnTranslationPipe} from '../../pipes/visualization/table-column-translation-pipe';
import {Tournament} from '../../models/tournament';
import {TournamentScore} from '../../models/tournament-score.model';
import {RoleType} from '../../models/role-type';

describe('TournamentListComponent', () => {
  let component: TournamentListComponent;
  let routerSpy: jasmine.SpyObj<Router>;
  let userSessionServiceSpy: jasmine.SpyObj<UserSessionService>;
  let tournamentServiceSpy: jasmine.SpyObj<TournamentService>;
  let rankingServiceSpy: jasmine.SpyObj<RankingService>;
  let messageServiceSpy: jasmine.SpyObj<MessageService>;
  let rbacServiceSpy: jasmine.SpyObj<RbacService>;
  let systemOverloadServiceMock: SystemOverloadService;
  let achievementsServiceSpy: jasmine.SpyObj<AchievementsService>;
  let translocoServiceSpy: jasmine.SpyObj<TranslocoService>;
  let biitSnackbarServiceSpy: jasmine.SpyObj<BiitSnackbarService>;
  let datePipe: DatePipe;
  let tableColumnTranslationPipeSpy: jasmine.SpyObj<TableColumnTranslationPipe>;

  const buildTournament = (id: number, createdAt: string): Tournament => {
    const t = new Tournament();
    t.id = id;
    t.name = `Tournament ${id}`;
    t.type = undefined as any;
    t.tournamentScore = new TournamentScore();
    t.shiaijos = 2;
    t.teamSize = 3;
    t.duelsDuration = 180;
    t.locked = false;
    t.createdAt = new Date(createdAt);
    t.updatedAt = new Date(createdAt);
    return t;
  };

  beforeEach(() => {
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    userSessionServiceSpy = jasmine.createSpyObj('UserSessionService', ['setSelectedTournament']);
    tournamentServiceSpy = jasmine.createSpyObj('TournamentService', [
      'getAll', 'delete', 'update', 'clone', 'getAccreditations', 'getDiplomas'
    ]);
    rankingServiceSpy = jasmine.createSpyObj('RankingService', ['getTournamentSummaryAsHtml', 'getAllListAsZip']);
    messageServiceSpy = jasmine.createSpyObj('MessageService', ['infoMessage', 'warningMessage']);
    rbacServiceSpy = jasmine.createSpyObj('RbacService', ['isAllowed']);
    achievementsServiceSpy = jasmine.createSpyObj('AchievementsService', ['regenerateTournamentAchievements']);
    translocoServiceSpy = jasmine.createSpyObj('TranslocoService', ['translate', 'selectTranslate']);
    biitSnackbarServiceSpy = jasmine.createSpyObj('BiitSnackbarService', ['showNotification']);
    tableColumnTranslationPipeSpy = jasmine.createSpyObj('TableColumnTranslationPipe', ['transform']);

    systemOverloadServiceMock = {
      isTransactionalBusy: { next: jasmine.createSpy('next') }
    } as unknown as SystemOverloadService;

    datePipe = new DatePipe('en-US');

    rbacServiceSpy.isAllowed.and.returnValue(true);
    translocoServiceSpy.translate.and.returnValue('translated');
    translocoServiceSpy.selectTranslate.and.returnValue(of('translated'));
    tournamentServiceSpy.getAll.and.returnValue(of([]));
    rankingServiceSpy.getTournamentSummaryAsHtml.and.returnValue(of(new Blob(['txt'])) as any);
    rankingServiceSpy.getAllListAsZip.and.returnValue(of(new Blob(['zip'])) as any);
    tournamentServiceSpy.getAccreditations.and.returnValue(of(new Blob(['pdf'])) as any);
    tournamentServiceSpy.getDiplomas.and.returnValue(of(new Blob(['pdf'])) as any);
    achievementsServiceSpy.regenerateTournamentAchievements.and.returnValue(of({}) as any);

    component = new TournamentListComponent(
      routerSpy,
      userSessionServiceSpy,
      tournamentServiceSpy,
      rankingServiceSpy,
      messageServiceSpy,
      rbacServiceSpy,
      systemOverloadServiceMock,
      achievementsServiceSpy,
      translocoServiceSpy,
      datePipe,
      biitSnackbarServiceSpy,
      tableColumnTranslationPipeSpy
    );
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should load and sort tournaments by createdAt descending', () => {
    const oldTournament = buildTournament(1, '2024-01-01');
    const newTournament = buildTournament(2, '2024-06-01');
    tournamentServiceSpy.getAll.and.returnValue(of([oldTournament, newTournament]));

    component.loadData();

    expect((systemOverloadServiceMock.isTransactionalBusy.next as jasmine.Spy)).toHaveBeenCalledWith(true);
    expect((component as any).tournaments[0].id).toBe(2);
    expect((component as any).tournaments[1].id).toBe(1);
    expect((systemOverloadServiceMock.isTransactionalBusy.next as jasmine.Spy)).toHaveBeenCalledWith(false);
  });

  it('should set defaults on addElement', () => {
    component.addElement();

    expect((component as any).target).toBeTruthy();
    expect((component as any).target.duelsDuration).toBe(Tournament.DEFAULT_DUELS_DURATION);
    expect((component as any).target.type).toBe(Tournament.DEFAULT_TYPE);
    expect((component as any).target.shiaijos).toBe(Tournament.DEFAULT_SHIAIJOS);
    expect((component as any).target.teamSize).toBe(Tournament.DEFAULT_TEAM_SIZE);
  });

  it('should set target and selected tournament on editElement', () => {
    const tournament = buildTournament(10, '2024-01-01');

    component.editElement(tournament);

    expect((component as any).target).toBe(tournament);
    expect(userSessionServiceSpy.setSelectedTournament).toHaveBeenCalledWith('10');
  });

  it('should delete tournaments and show success notification', () => {
    const t1 = buildTournament(1, '2024-01-01');
    const t2 = buildTournament(2, '2024-01-02');
    tournamentServiceSpy.delete.and.returnValues(of(t1), of(t2));
    translocoServiceSpy.selectTranslate.and.returnValue(of('deleted'));
    spyOn(component, 'loadData');

    component.deleteElements([t1, t2]);

    expect(tournamentServiceSpy.delete).toHaveBeenCalledTimes(2);
    expect(component.loadData).toHaveBeenCalled();
    expect((component as any).confirmDelete).toBeFalse();
    expect(biitSnackbarServiceSpy.showNotification).toHaveBeenCalledWith('deleted', NotificationType.SUCCESS);
  });

  it('should open fights for tournament', () => {
    const tournament = buildTournament(7, '2024-01-01');

    component.openFights(tournament);

    expect(userSessionServiceSpy.setSelectedTournament).toHaveBeenCalledWith('7');
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/tournaments/fights'], { state: { tournamentId: 7 } });
  });

  it('should open statistics for tournament', () => {
    const tournament = buildTournament(8, '2024-01-01');

    component.openStatistics(tournament);

    expect(userSessionServiceSpy.setSelectedTournament).toHaveBeenCalledWith('8');
    expect(routerSpy.navigate).toHaveBeenCalledWith(['/tournaments/statistics'], { state: { tournamentId: 8 } });
  });

  it('should clone tournament and close confirmClone', () => {
    const tournament = buildTournament(9, '2024-01-01');
    tournamentServiceSpy.clone.and.returnValue(of(tournament));
    spyOn(component, 'loadData');

    component.cloneElement(tournament);

    expect(tournamentServiceSpy.clone).toHaveBeenCalledWith(9);
    expect(component.loadData).toHaveBeenCalled();
    expect(messageServiceSpy.infoMessage).toHaveBeenCalledWith('infoTournamentStored');
    expect((component as any).confirmClone).toBeFalse();
  });

  it('should update tournament lock and notify', () => {
    const tournament = buildTournament(3, '2024-01-01');
    tournament.locked = false;
    tournamentServiceSpy.update.and.returnValue(of(tournament));
    spyOn(component, 'loadData');

    component.lockElement(tournament, true);

    expect(achievementsServiceSpy.regenerateTournamentAchievements).toHaveBeenCalledWith(3);
    expect(tournamentServiceSpy.update).toHaveBeenCalled();
    expect(component.loadData).toHaveBeenCalled();
    expect(messageServiceSpy.infoMessage).toHaveBeenCalledWith('infoTournamentUpdated');
  });

  it('should set selected tournament when exactly one selected', () => {
    const tournament = buildTournament(11, '2024-01-01');

    component.selectTournaments([tournament]);

    expect(userSessionServiceSpy.setSelectedTournament).toHaveBeenCalledWith('11');
  });

  it('should clear selected tournament when none or many selected', () => {
    const t1 = buildTournament(1, '2024-01-01');
    const t2 = buildTournament(2, '2024-01-02');

    component.selectTournaments([]);
    component.selectTournaments([t1, t2]);

    expect(userSessionServiceSpy.setSelectedTournament).toHaveBeenCalledWith(undefined);
  });

  it('should return joined tournament names', () => {
    const t1 = buildTournament(1, '2024-01-01');
    const t2 = buildTournament(2, '2024-01-02');

    expect(component.getTournamentNames([t1, t2])).toBe('Tournament 1, Tournament 2');
    expect(component.getTournamentNames(undefined as any)).toBe('');
  });

  it('should show notification and clear target on onSaved', () => {
    const t = buildTournament(1, '2024-01-01');
    spyOn(component, 'loadData');
    (component as any).target = t;

    component.onSaved(t);

    expect(biitSnackbarServiceSpy.showNotification).toHaveBeenCalledWith('translated', NotificationType.INFO);
    expect(component.loadData).toHaveBeenCalledWith(t);
    expect((component as any).target).toBeNull();
  });

  it('should request files for download methods when tournament has id', () => {
    const t = buildTournament(4, '2024-01-01');
    spyOn(URL, 'createObjectURL').and.returnValue('blob:url');
    const anchor = { click: jasmine.createSpy('click') } as any;
    spyOn(document, 'createElement').and.returnValue(anchor);

    component.downloadBlogCode(t);
    component.downloadZip(t);
    component.downloadAccreditations({ tournament: t, roles: [RoleType.COMPETITOR], newOnes: false });
    component.downloadDiplomas({ tournament: t, roles: [RoleType.COMPETITOR], newOnes: true });

    expect(rankingServiceSpy.getTournamentSummaryAsHtml).toHaveBeenCalledWith(4);
    expect(rankingServiceSpy.getAllListAsZip).toHaveBeenCalledWith(4);
    expect(tournamentServiceSpy.getAccreditations).toHaveBeenCalledWith(4, false, [RoleType.COMPETITOR]);
    expect(tournamentServiceSpy.getDiplomas).toHaveBeenCalledWith(4, true, [RoleType.COMPETITOR]);
    expect(anchor.click).toHaveBeenCalled();
  });
}
);
