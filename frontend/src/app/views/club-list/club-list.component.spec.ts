import {of} from 'rxjs';
import {BiitSnackbarService, NotificationType} from '@biit-solutions/wizardry-theme/info';
import {ClubListComponent} from './club-list.component';
import {ClubService} from '../../services/club.service';
import {TranslocoService} from '@ngneat/transloco';
import {RbacService} from '../../services/rbac/rbac.service';
import {DatePipe} from '@angular/common';
import {SystemOverloadService} from '../../services/notifications/system-overload.service';
import {Club} from '../../models/club';

describe('ClubListComponent', () => {
  let component: ClubListComponent;
  let clubServiceSpy: jasmine.SpyObj<ClubService>;
  let translocoServiceSpy: jasmine.SpyObj<TranslocoService>;
  let rbacServiceSpy: jasmine.SpyObj<RbacService>;
  let systemOverloadServiceSpy: jasmine.SpyObj<SystemOverloadService>;
  let biitSnackbarServiceSpy: jasmine.SpyObj<BiitSnackbarService>;
  let datePipeSpy: jasmine.SpyObj<DatePipe>;

  const testClub: Club = {
    id: 1,
    name: 'Kendo Club A',
    country: 'Spain',
    city: 'Madrid',
    address: 'Calle Principal 123',
    email: 'club@example.com',
    phone: '+34123456789',
    web: 'www.club.com'
  } as Club;

  const testClub2: Club = {
    id: 2,
    name: 'Kendo Club B',
    country: 'France',
    city: 'Paris',
    address: 'Rue de la Paix 456',
    email: 'club2@example.com',
    phone: '+33123456789',
    web: 'www.club2.com'
  } as Club;

  beforeEach(async () => {
    clubServiceSpy = jasmine.createSpyObj('ClubService', ['getAll', 'delete']);
    translocoServiceSpy = jasmine.createSpyObj('TranslocoService', ['selectTranslate', 'translate']);
    rbacServiceSpy = jasmine.createSpyObj('RbacService', ['isAllowed']);
    systemOverloadServiceSpy = jasmine.createSpyObj('SystemOverloadService', [], {
      isTransactionalBusy: {next: jasmine.createSpy('next')}
    });
    biitSnackbarServiceSpy = jasmine.createSpyObj('BiitSnackbarService', ['showNotification']);
    datePipeSpy = jasmine.createSpyObj('DatePipe', ['transform']);

    translocoServiceSpy.selectTranslate.and.returnValue(of('translated'));
    translocoServiceSpy.translate.and.returnValue('translated');
    datePipeSpy.transform.and.returnValue('01/01/2024');
    rbacServiceSpy.isAllowed.and.returnValue(true);

    component = new ClubListComponent(
      clubServiceSpy,
      translocoServiceSpy,
      rbacServiceSpy,
      datePipeSpy,
      systemOverloadServiceSpy,
      biitSnackbarServiceSpy
    );
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize columns with transloco values in ngAfterViewInit', () => {
    clubServiceSpy.getAll.and.returnValue(of([]));
    translocoServiceSpy.selectTranslate.and.returnValue(of('translated'));

    component.ngAfterViewInit();

    expect(translocoServiceSpy.selectTranslate).toHaveBeenCalledWith('id');
    expect(translocoServiceSpy.selectTranslate).toHaveBeenCalledWith('name');
    expect(translocoServiceSpy.selectTranslate).toHaveBeenCalledWith('country');
    expect(translocoServiceSpy.selectTranslate).toHaveBeenCalledWith('city');
    expect((component as any).columns.length).toBeGreaterThan(0);
  });

  it('should call loadData after columns are initialized', () => {
    clubServiceSpy.getAll.and.returnValue(of([]));
    spyOn(component, 'loadData');

    component.ngAfterViewInit();

    expect(component.loadData).toHaveBeenCalled();
  });

  it('should set busy flag and load clubs in loadData', () => {
    clubServiceSpy.getAll.and.returnValue(of([testClub, testClub2]));

    component.loadData();

    expect(systemOverloadServiceSpy.isTransactionalBusy.next).toHaveBeenCalledWith(true);
    expect((component as any).clubs.length).toBe(2);
  });

  it('should clone clubs in loadData', () => {
    clubServiceSpy.getAll.and.returnValue(of([testClub]));
    spyOn(Club, 'clone').and.callThrough();

    component.loadData();

    expect((component as any).clubs.length).toBe(1);
  });

  it('should set loading flag to false after loadData completes', (done) => {
    clubServiceSpy.getAll.and.returnValue(of([testClub]));

    component.loadData();

    setTimeout(() => {
      expect((component as any).loading).toBeFalse();
      expect(systemOverloadServiceSpy.isTransactionalBusy.next).toHaveBeenCalledWith(false);
      done();
    }, 100);
  });

  it('should create new club when addElement is called', () => {
    (component as any).addElement();

    expect((component as any).target).toBeTruthy();
    expect((component as any).target.id).toBeUndefined();
  });

  it('should set target club when editElement is called', () => {
    (component as any).editElement(testClub);

    expect((component as any).target).toEqual(testClub);
  });

  it('should delete multiple clubs and reload data on success', () => {
    clubServiceSpy.getAll.and.returnValue(of([]));
    clubServiceSpy.delete.and.returnValues(of(testClub), of(testClub2));
    translocoServiceSpy.selectTranslate.and.returnValue(of('Club deleted'));

    component.loadData();
    (component as any).deleteElements([testClub, testClub2]);

    expect(clubServiceSpy.delete).toHaveBeenCalledTimes(2);
    expect((component as any).confirmDelete).toBeFalse();
    expect(biitSnackbarServiceSpy.showNotification).toHaveBeenCalledWith(
      'Club deleted',
      NotificationType.SUCCESS
    );
  });

  it('should not delete clubs when list is empty', () => {
    (component as any).deleteElements(null);

    expect(clubServiceSpy.delete).not.toHaveBeenCalled();
  });

  it('should reload data and clear target on onSaved', () => {
    clubServiceSpy.getAll.and.returnValue(of([]));
    spyOn(component, 'loadData');

    (component as any).onSaved(testClub);

    expect(biitSnackbarServiceSpy.showNotification).toHaveBeenCalledWith(
      'translated',
      NotificationType.INFO
    );
    expect(component.loadData).toHaveBeenCalled();
    expect((component as any).target).toBeNull();
  });

  it('should format club names correctly in getClubNames', () => {
    const names = (component as any).getClubNames([testClub, testClub2]);

    expect(names).toBe('Kendo Club A, Kendo Club B');
  });

  it('should return empty string when clubs list is null in getClubNames', () => {
    const names = (component as any).getClubNames(null);

    expect(names).toBe('');
  });

  it('should return empty string when clubs list is empty in getClubNames', () => {
    const names = (component as any).getClubNames([]);

    expect(names).toBe('');
  });
});

