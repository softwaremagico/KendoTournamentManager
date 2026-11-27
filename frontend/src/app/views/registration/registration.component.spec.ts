import {ActivatedRoute} from '@angular/router';
import {of} from 'rxjs';
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

  it('loads the public configuration using the invitation query parameter', () => {
    createComponent('jwt.invitation');
    registrationService.configuration.and.returnValue(of(configuration));

    component.ngOnInit();

    expect(registrationService.configuration).toHaveBeenCalledOnceWith('jwt.invitation');
    expect(component.configuration).toBe(configuration);
    expect(component.loading).toBeFalse();
  });

  it('does not call the backend when the invitation is missing', () => {
    createComponent(undefined);

    component.ngOnInit();

    expect(registrationService.configuration).not.toHaveBeenCalled();
    expect(component.errorMessage).toBe('registrationInvalidInvitation');
  });

  it('submits only public registration fields with the selected club', () => {
    createComponent('jwt.invitation');
    registrationService.configuration.and.returnValue(of(configuration));
    component.ngOnInit();

    component.request.name = 'Aki';
    component.request.lastname = 'Takeshi';
    component.request.idCard = '12345678Z';
    component.selectedClubId = 7;
    component.toggleRole(RoleType.COMPETITOR, true);
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

  it('omits team preference when team selection is disabled', () => {
    createComponent('jwt.invitation');
    registrationService.configuration.and.returnValue(of({...configuration, teamSelectionEnabled: false}));
    component.ngOnInit();

    component.request.name = 'Aki';
    component.request.lastname = 'Takeshi';
    component.request.idCard = '12345678Z';
    component.selectedClubId = 7;
    component.toggleRole(RoleType.REFEREE, true);
    component.request.teamPreference = 'Team A';
    registrationService.register.and.returnValue(of({
      name: 'Aki', lastname: 'Takeshi', roles: [RoleType.REFEREE], existingParticipant: false, pendingReview: true
    }));

    component.submit();

    const request: RegistrationRequest = registrationService.register.calls.mostRecent().args[1];
    expect(request.teamPreference).toBeUndefined();
    expect(component.registered?.pendingReview).toBeTrue();
  });

  it('does not submit an incomplete form', () => {
    createComponent('jwt.invitation');
    registrationService.configuration.and.returnValue(of(configuration));
    component.ngOnInit();

    component.submit();

    expect(registrationService.register).not.toHaveBeenCalled();
  });
});
