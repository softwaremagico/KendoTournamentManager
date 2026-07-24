import {of} from 'rxjs';
import {BiitSnackbarService, NotificationType} from '@biit-solutions/wizardry-theme/info';
import {ParticipantListComponent} from './participant-list.component';
import {ParticipantService} from '../../services/participant.service';
import {ClubService} from '../../services/club.service';
import {TranslocoService} from '@jsverse/transloco';
import {RbacService} from '../../services/rbac/rbac.service';
import {Router} from '@angular/router';
import {UserSessionService} from '../../services/user-session.service';
import {DatePipe} from '@angular/common';
import {SystemOverloadService} from '../../services/notifications/system-overload.service';
import {ClubNamePipe} from '../../pipes/visualization/club-name-pipe';
import {Participant} from '../../models/participant';
import {Club} from '../../models/club';

describe('ParticipantListComponent', () => {
  let component: ParticipantListComponent;
  let participantServiceSpy: jasmine.SpyObj<ParticipantService>;
  let clubServiceSpy: jasmine.SpyObj<ClubService>;
  let translocoServiceSpy: jasmine.SpyObj<TranslocoService>;
  let rbacServiceSpy: jasmine.SpyObj<RbacService>;
  let routerSpy: jasmine.SpyObj<Router>;
  let userSessionServiceSpy: jasmine.SpyObj<UserSessionService>;
  let systemOverloadServiceSpy: jasmine.SpyObj<SystemOverloadService>;
  let biitSnackbarServiceSpy: jasmine.SpyObj<BiitSnackbarService>;
  let datePipeSpy: jasmine.SpyObj<DatePipe>;
  let clubNamePipeSpy: jasmine.SpyObj<ClubNamePipe>;

  const testClub: Club = {
    id: 1,
    name: 'Kendo Club A'
  } as Club;

  const testParticipant: Participant = {
    id: 1,
    name: 'John',
    lastname: 'Doe',
    idCard: 'ABC123',
    club: testClub
  } as Participant;

  const testParticipant2: Participant = {
    id: 2,
    name: 'Jane',
    lastname: 'Smith',
    idCard: 'XYZ789',
    club: testClub
  } as Participant;

  beforeEach(async () => {
    participantServiceSpy = jasmine.createSpyObj('ParticipantService', ['getAll', 'delete']);
    clubServiceSpy = jasmine.createSpyObj('ClubService', ['getAll']);
    translocoServiceSpy = jasmine.createSpyObj('TranslocoService', ['selectTranslate', 'translate']);
    rbacServiceSpy = jasmine.createSpyObj('RbacService', ['isAllowed']);
    routerSpy = jasmine.createSpyObj('Router', ['navigate']);
    userSessionServiceSpy = jasmine.createSpyObj('UserSessionService', ['setSelectedParticipant']);
    systemOverloadServiceSpy = jasmine.createSpyObj('SystemOverloadService', [], {
      isTransactionalBusy: { next: jasmine.createSpy('next') }
    });
    biitSnackbarServiceSpy = jasmine.createSpyObj('BiitSnackbarService', ['showNotification']);
    datePipeSpy = jasmine.createSpyObj('DatePipe', ['transform']);
    clubNamePipeSpy = jasmine.createSpyObj('ClubNamePipe', ['transform']);

    translocoServiceSpy.selectTranslate.and.returnValue(of('translated'));
    translocoServiceSpy.translate.and.returnValue('translated');
    datePipeSpy.transform.and.returnValue('01/01/2024');
    clubNamePipeSpy.transform.and.returnValue('Club A');
    rbacServiceSpy.isAllowed.and.returnValue(true);

    component = new ParticipantListComponent(
      routerSpy,
      userSessionServiceSpy,
      participantServiceSpy,
      clubServiceSpy,
      translocoServiceSpy,
      rbacServiceSpy,
      datePipeSpy,
      clubNamePipeSpy,
      systemOverloadServiceSpy,
      biitSnackbarServiceSpy
    );
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize columns with transloco values in ngAfterViewInit', () => {
    participantServiceSpy.getAll.and.returnValue(of([]));
    clubServiceSpy.getAll.and.returnValue(of([]));

    translocoServiceSpy.selectTranslate.and.returnValue(of('translated'));

    component.ngAfterViewInit();

    expect(translocoServiceSpy.selectTranslate).toHaveBeenCalledWith('id');
    expect(translocoServiceSpy.selectTranslate).toHaveBeenCalledWith('idCard');
    expect(translocoServiceSpy.selectTranslate).toHaveBeenCalledWith('name');
    expect(translocoServiceSpy.selectTranslate).toHaveBeenCalledWith('lastname');
    expect(translocoServiceSpy.selectTranslate).toHaveBeenCalledWith('club');
    expect((component as any).columns.length).toBeGreaterThan(0);
  });

  it('should set busy flag and load clubs in ngAfterViewInit', () => {
    participantServiceSpy.getAll.and.returnValue(of([]));
    clubServiceSpy.getAll.and.returnValue(of([testClub]));

    component.ngAfterViewInit();

    expect(systemOverloadServiceSpy.isTransactionalBusy.next).toHaveBeenCalledWith(true);
    expect((component as any).clubs).toEqual([testClub]);
  });

  it('should load participants and map them to cloned instances', () => {
    participantServiceSpy.getAll.and.returnValue(of([testParticipant, testParticipant2]));
    clubServiceSpy.getAll.and.returnValue(of([]));
    spyOn(Participant, 'clone').and.callThrough();

    component.ngAfterViewInit();

    expect(participantServiceSpy.getAll).toHaveBeenCalled();
    expect((component as any).participants.length).toBe(2);
  });

  it('should sort clubs by name in loadData', () => {
    const club1: Club = { id: 1, name: 'Zulu Club' } as Club;
    const club2: Club = { id: 2, name: 'Alpha Club' } as Club;
    const club3: Club = { id: 3, name: 'Beta Club' } as Club;

    clubServiceSpy.getAll.and.returnValue(of([club1, club2, club3]));
    participantServiceSpy.getAll.and.returnValue(of([]));

    component.ngAfterViewInit();

    expect((component as any).clubs[0].name).toBe('Alpha Club');
    expect((component as any).clubs[1].name).toBe('Beta Club');
    expect((component as any).clubs[2].name).toBe('Zulu Club');
  });

  it('should set loading flag to false after loadData completes', (done) => {
    participantServiceSpy.getAll.and.returnValue(of([testParticipant]));
    clubServiceSpy.getAll.and.returnValue(of([]));

    component.ngAfterViewInit();

    setTimeout(() => {
      expect((component as any).loading).toBeFalse();
      expect(systemOverloadServiceSpy.isTransactionalBusy.next).toHaveBeenCalledWith(false);
      done();
    }, 100);
  });

  it('should create new participant when addElement is called', () => {
    component.ngAfterViewInit.bind(component);
    (component as any).addElement();

    expect((component as any).target).toBeTruthy();
    expect((component as any).target.id).toBeUndefined();
  });

  it('should set target participant when editElement is called', () => {
    component.ngAfterViewInit.bind(component);
    (component as any).editElement(testParticipant);

    expect((component as any).target).toEqual(testParticipant);
  });

  it('should delete multiple participants and reload data on success', () => {
    participantServiceSpy.getAll.and.returnValue(of([]));
    participantServiceSpy.delete.and.returnValues(of(testParticipant), of(testParticipant2));
    clubServiceSpy.getAll.and.returnValue(of([]));
    translocoServiceSpy.selectTranslate.and.returnValue(of('Participants deleted'));

    component.ngAfterViewInit();
    (component as any).deleteElements([testParticipant, testParticipant2]);

    expect(participantServiceSpy.delete).toHaveBeenCalledTimes(2);
    expect((component as any).confirmDelete).toBeFalse();
    expect(biitSnackbarServiceSpy.showNotification).toHaveBeenCalledWith(
      'Participants deleted',
      NotificationType.SUCCESS
    );
  });

  it('should not delete participants when list is empty', () => {
    component.ngAfterViewInit.bind(component);
    (component as any).deleteElements(null);

    expect(participantServiceSpy.delete).not.toHaveBeenCalled();
  });

  it('should open statistics and navigate with participant id', () => {
    component.ngAfterViewInit.bind(component);
    (component as any).openStatistics(testParticipant);

    expect(userSessionServiceSpy.setSelectedParticipant).toHaveBeenCalledWith('1');
    expect(routerSpy.navigate).toHaveBeenCalledWith(
      ['/participants/statistics'],
      { state: { participantId: 1 } }
    );
  });

  it('should not navigate when participant is null in openStatistics', () => {
    component.ngAfterViewInit.bind(component);
    (component as any).openStatistics(null);

    expect(routerSpy.navigate).not.toHaveBeenCalled();
  });

  it('should reload data and clear target on onSaved', () => {
    participantServiceSpy.getAll.and.returnValue(of([]));
    clubServiceSpy.getAll.and.returnValue(of([]));

    component.ngAfterViewInit();
    (component as any).onSaved(testParticipant);

    expect(biitSnackbarServiceSpy.showNotification).toHaveBeenCalledWith(
      'translated',
      NotificationType.INFO
    );
    expect((component as any).target).toBeNull();
  });

  it('should format participant names correctly in getParticipantNames', () => {
    const names = (component as any).getParticipantNames([testParticipant, testParticipant2]);

    expect(names).toBe('John Doe, Jane Smith');
  });

  it('should return empty string when participants list is null in getParticipantNames', () => {
    const names = (component as any).getParticipantNames(null);

    expect(names).toBe('');
  });

  it('should return empty string when participants list is empty in getParticipantNames', () => {
    const names = (component as any).getParticipantNames([]);

    expect(names).toBe('');
  });
});



