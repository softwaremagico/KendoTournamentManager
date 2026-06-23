import {of} from 'rxjs';
import {BiitSnackbarService, NotificationType} from '@biit-solutions/wizardry-theme/info';
import {ClubFormComponent} from './club-form.component';
import {RbacService} from '../../services/rbac/rbac.service';
import {TranslocoService} from '@ngneat/transloco';
import {ClubService} from '../../services/club.service';
import {Club} from '../../models/club';
import {ClubFormValidationFields} from './club-form-validation-fields';

describe('ClubFormComponent', () => {
  let component: ClubFormComponent;
  let rbacServiceSpy: jasmine.SpyObj<RbacService>;
  let translocoServiceSpy: jasmine.SpyObj<TranslocoService>;
  let biitSnackbarServiceSpy: jasmine.SpyObj<BiitSnackbarService>;
  let clubServiceSpy: jasmine.SpyObj<ClubService>;

  const buildValidClub = (): Club => ({
    id: 1,
    name: 'Kendo Club',
    country: 'Spain',
    city: 'Madrid',
    address: '123 Main St',
    email: 'club@example.com',
    phone: '123456789',
    web: 'https://www.club.com'
  } as Club);

  beforeEach(() => {
    rbacServiceSpy = jasmine.createSpyObj('RbacService', ['isAllowed']);
    translocoServiceSpy = jasmine.createSpyObj('TranslocoService', ['translate']);
    biitSnackbarServiceSpy = jasmine.createSpyObj('BiitSnackbarService', ['showNotification']);
    clubServiceSpy = jasmine.createSpyObj('ClubService', ['add', 'update']);

    rbacServiceSpy.isAllowed.and.returnValue(true);
    translocoServiceSpy.translate.and.returnValue('validation error');

    component = new ClubFormComponent(
      rbacServiceSpy,
      translocoServiceSpy,
      biitSnackbarServiceSpy,
      clubServiceSpy
    );
    component.club = buildValidClub();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with empty errors map', () => {
    expect((component as any).errors.size).toBe(0);
  });

  it('should validate successfully with a valid club', () => {
    const result = (component as any).validate();

    expect(result).toBeTrue();
    expect((component as any).errors.size).toBe(0);
  });

  it('should fail validation when club name is missing', () => {
    component.club.name = '';

    const result = (component as any).validate();

    expect(result).toBeFalse();
    expect((component as any).errors.has(ClubFormValidationFields.NAME_ERRORS)).toBeTrue();
  });

  it('should fail validation when club country is missing', () => {
    component.club.country = '';

    const result = (component as any).validate();

    expect(result).toBeFalse();
    expect((component as any).errors.has(ClubFormValidationFields.COUNTRY_ERRORS)).toBeTrue();
  });

  it('should fail validation when club city is missing', () => {
    component.club.city = '';

    const result = (component as any).validate();

    expect(result).toBeFalse();
    expect((component as any).errors.has(ClubFormValidationFields.CITY_ERRORS)).toBeTrue();
  });

  it('should fail validation with invalid email format', () => {
    component.club.email = 'invalid-email';

    const result = (component as any).validate();

    expect(result).toBeFalse();
    expect((component as any).errors.has(ClubFormValidationFields.EMAIL_ERRORS)).toBeTrue();
  });

  it('should pass validation when email is empty (optional)', () => {
    component.club.email = undefined as any;

    const result = (component as any).validate();

    expect(result).toBeTrue();
  });

  it('should call update service when club has id on onSave', () => {
    clubServiceSpy.update.and.returnValue(of(component.club));
    spyOn(component.saved, 'emit');

    component.onSave();

    expect(clubServiceSpy.update).toHaveBeenCalledWith(component.club);
    expect(component.saved.emit).toHaveBeenCalled();
  });

  it('should call add service when club has no id on onSave', () => {
    component.club.id = undefined as any;
    clubServiceSpy.add.and.returnValue(of(component.club));
    spyOn(component.saved, 'emit');

    component.onSave();

    expect(clubServiceSpy.add).toHaveBeenCalledWith(component.club);
    expect(component.saved.emit).toHaveBeenCalled();
  });

  it('should show warning notification when validation fails on onSave', () => {
    component.club.name = '';

    component.onSave();

    expect(biitSnackbarServiceSpy.showNotification).toHaveBeenCalledWith(
      'validation error',
      NotificationType.WARNING
    );
    expect(clubServiceSpy.update).not.toHaveBeenCalled();
    expect(clubServiceSpy.add).not.toHaveBeenCalled();
  });

  it('should set saving to false after onSave completes', (done) => {
    clubServiceSpy.update.and.returnValue(of(component.club));

    component.onSave();

    setTimeout(() => {
      expect((component as any).saving).toBeFalse();
      done();
    }, 100);
  });
});

