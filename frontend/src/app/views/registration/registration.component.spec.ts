import {ActivatedRoute} from '@angular/router';
import {of, Subject} from 'rxjs';
import {RoleType} from '../../models/role-type';
import {RegistrationService} from '../../services/registration.service';
import {RegistrationConfiguration, RegistrationRequest, RegistrationResponse} from '../../services/models/registration.model';
import {RegistrationComponent} from './registration.component';

describe('RegistrationComponent', () => {
  let component: RegistrationComponent;
  let route: jasmine.SpyObj<ActivatedRoute>;
  let registrationService: jasmine.SpyObj<RegistrationService>;

  const configuration: RegistrationConfiguration = {
    tournamentName: 'Open Cup',
    teamSize: 3,
    teamSelectionEnabled: true,
    clubs: [{id: 7, name: 'Kendo Club', city: 'Barcelona', country: 'Spain'}],
    availableRoles: [RoleType.COMPETITOR, RoleType.REFEREE]
  };

  function createComponent(invitation: string | undefined): void {
    route = jasmine.createSpyObj('ActivatedRoute', [], {queryParams: of({invitation})});
    registrationService = jasmine.createSpyObj('RegistrationService', ['configuration', 'register']);
    component = new RegistrationComponent(route, registrationService);
  }

  function fillRequiredFields(): void {
    component.request.name = 'Aki';
    component.request.lastname = 'Takeshi';
    component.request.idCard = '12345678Z';
    component.selectedClubId = 7;
  }

  it('loads the public configuration using the invitation query parameter', () => {
    createComponent('jwt.invitation');
    registrationService.configuration.and.returnValue(of(configuration));

    component.ngOnInit();

    expect(registrationService.configuration).toHaveBeenCalledOnceWith('jwt.invitation');
    expect(component.configuration).toBe(configuration);
    expect(component.loading).toBeFalse();
  });

  it('shows an error when the invitation cannot be resolved', () => {
    createComponent('jwt.invitation');
    registrationService.configuration.and.returnValue(of(undefined as unknown as RegistrationConfiguration));

    component.ngOnInit();

    expect(component.loading).toBeFalse();
    expect(component.errorMessage).toBe('registrationError');
  });

  it('does not call the backend when the invitation is missing', () => {
    createComponent(undefined);

    component.ngOnInit();

    expect(registrationService.configuration).not.toHaveBeenCalled();
    expect(component.errorMessage).toBe('registrationInvalidInvitation');
  });

  it('uses lowercase translation keys for roles', () => {
    createComponent('jwt.invitation');

    expect(component.roleKey(RoleType.COMPETITOR)).toBe('competitor');
  });

  it('selects and deselects roles without duplicating them', () => {
    createComponent('jwt.invitation');

    expect(component.isRoleSelected(RoleType.REFEREE)).toBeFalse();
    component.selectRole(RoleType.REFEREE);
    component.selectRole(RoleType.REFEREE);
    expect(component.request.roles).toEqual([RoleType.REFEREE]);
    expect(component.isRoleSelected(RoleType.REFEREE)).toBeTrue();

    component.deselectRole(RoleType.REFEREE);
    expect(component.request.roles).toEqual([]);
  });

  it('submits only public registration fields with the selected club', () => {
    createComponent('jwt.invitation');
    registrationService.configuration.and.returnValue(of(configuration));
    component.ngOnInit();

    fillRequiredFields();
    component.selectRole(RoleType.COMPETITOR);
    component.request.teamPreference = 'Team A';
    registrationService.register.and.returnValue(of({
      name: 'Aki', lastname: 'Takeshi', roles: [RoleType.COMPETITOR], existingParticipant: false, pendingReview: false
    }));

    component.submit();

    const request: RegistrationRequest = registrationService.register.calls.mostRecent().args[1];
    expect(registrationService.register.calls.mostRecent().args[0]).toBe('jwt.invitation');
    expect(request).toEqual(jasmine.objectContaining({
      idCard: '12345678Z', name: 'Aki', lastname: 'Takeshi', clubId: 7, teamPreference: 'Team A'
    }));
    expect(request.newClubName).toBeUndefined();
    expect((request as unknown as {password?: string}).password).toBeUndefined();
    expect(component.registered?.name).toBe('Aki');
  });

  it('submits a new club with trimmed fields', () => {
    createComponent('jwt.invitation');
    registrationService.configuration.and.returnValue(of(configuration));
    component.ngOnInit();

    fillRequiredFields();
    component.useExistingClub = false;
    component.request.newClubName = '  New Club  ';
    component.request.newClubCity = '  Madrid  ';
    component.request.newClubCountry = '  Spain  ';
    component.selectRole(RoleType.REFEREE);
    registrationService.register.and.returnValue(of({
      name: 'Aki', lastname: 'Takeshi', roles: [RoleType.REFEREE], existingParticipant: true, pendingReview: true
    }));

    component.submit();

    const request: RegistrationRequest = registrationService.register.calls.mostRecent().args[1];
    expect(request.clubId).toBeUndefined();
    expect(request.newClubName).toBe('New Club');
    expect(request.newClubCity).toBe('Madrid');
    expect(request.newClubCountry).toBe('Spain');
    expect(component.registered?.pendingReview).toBeTrue();
  });

  it('omits team preference when team selection is disabled', () => {
    createComponent('jwt.invitation');
    registrationService.configuration.and.returnValue(of({...configuration, teamSelectionEnabled: false}));
    component.ngOnInit();

    fillRequiredFields();
    component.selectRole(RoleType.REFEREE);
    component.request.teamPreference = 'Team A';
    registrationService.register.and.returnValue(of({
      name: 'Aki', lastname: 'Takeshi', roles: [RoleType.REFEREE], existingParticipant: false, pendingReview: true
    }));

    component.submit();

    const request: RegistrationRequest = registrationService.register.calls.mostRecent().args[1];
    expect(request.teamPreference).toBeUndefined();
    expect(component.registered?.pendingReview).toBeTrue();
  });

  it('shows an error when registration fails', () => {
    createComponent('jwt.invitation');
    registrationService.configuration.and.returnValue(of(configuration));
    component.ngOnInit();

    fillRequiredFields();
    component.selectRole(RoleType.COMPETITOR);
    registrationService.register.and.returnValue(of(undefined as unknown as RegistrationResponse));

    component.submit();

    expect(component.errorMessage).toBe('registrationSubmitError');
    expect(component.submitting).toBeFalse();
    expect(component.registered).toBeUndefined();
  });

  it('does not submit an incomplete form or a second submission', () => {
    createComponent('jwt.invitation');
    registrationService.configuration.and.returnValue(of(configuration));
    component.ngOnInit();

    component.submit();
    expect(registrationService.register).not.toHaveBeenCalled();

    fillRequiredFields();
    component.selectRole(RoleType.COMPETITOR);
    const response: Subject<RegistrationResponse> = new Subject<RegistrationResponse>();
    registrationService.register.and.returnValue(response);
    component.submit();
    component.submit();

    expect(registrationService.register).toHaveBeenCalledTimes(1);
  });

  it('requires a club when using the new-club form', () => {
    createComponent('jwt.invitation');

    component.request.name = 'Aki';
    component.request.lastname = 'Takeshi';
    component.request.idCard = '12345678Z';
    component.selectRole(RoleType.COMPETITOR);
    component.useExistingClub = false;

    expect(component.isValid()).toBeFalse();
    component.request.newClubName = 'New Club';
    component.request.newClubCity = 'Madrid';
    component.request.newClubCountry = 'Spain';
    expect(component.isValid()).toBeTrue();
  });
});
