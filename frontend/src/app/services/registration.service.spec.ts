import {HttpClient} from '@angular/common/http';
import {of} from 'rxjs';
import {EnvironmentService} from '../environment.service';
import {RoleType} from '../models/role-type';
import {LoggerService} from './logger.service';
import {MessageService} from './message.service';
import {SystemOverloadService} from './notifications/system-overload.service';
import {RegistrationService} from './registration.service';
import {RegistrationConfiguration, RegistrationRequest, RegistrationResponse} from './models/registration.model';

describe('RegistrationService', () => {
  let http: jasmine.SpyObj<HttpClient>;
  let environment: jasmine.SpyObj<EnvironmentService>;
  let messages: jasmine.SpyObj<MessageService>;
  let logger: jasmine.SpyObj<LoggerService>;
  let busy: SystemOverloadService;
  let service: RegistrationService;

  beforeEach(() => {
    http = jasmine.createSpyObj<HttpClient>('HttpClient', ['get', 'post']);
    (http.get as jasmine.Spy).and.returnValue(of({tournamentName: 'Cup', clubs: [], availableRoles: ['COMPETITOR']}));
    (http.post as jasmine.Spy).and.returnValue(of({name: 'Aki'}));
    environment = jasmine.createSpyObj<EnvironmentService>('EnvironmentService', ['getBackendUrl']);
    environment.getBackendUrl.and.returnValue('/backend');
    messages = jasmine.createSpyObj<MessageService>('MessageService', ['handleError']);
    (messages.handleError as jasmine.Spy).and.returnValue(() => of(undefined));
    logger = jasmine.createSpyObj<LoggerService>('LoggerService', ['info']);
    busy = {isBusy: jasmine.createSpyObj('isBusy', ['next'])} as unknown as SystemOverloadService;
    service = new RegistrationService(http, environment, messages, logger, busy);
  });

  it('requests the public configuration with the invitation as query parameter', () => {
    let configuration: RegistrationConfiguration | undefined;
    service.configuration('jwt.invitation').subscribe(value => configuration = value);

    expect(http.get).toHaveBeenCalledWith('/backend/registration/public/configuration?invitation=jwt.invitation');
    expect(configuration?.tournamentName).toBe('Cup');
  });

  it('posts the public registration request with the invitation as query parameter', () => {
    const request: RegistrationRequest = {
      idCard: '1234', name: 'Aki', lastname: 'Takeshi', roles: [RoleType.COMPETITOR], clubId: 3
    };
    let response: RegistrationResponse | undefined;
    service.register('jwt.invitation', request).subscribe(value => response = value);

    expect(http.post).toHaveBeenCalledWith('/backend/registration/public/register?invitation=jwt.invitation', request);
    expect(response?.name).toBe('Aki');
  });

  it('encodes the invitation and releases the busy state', () => {
    service.configuration('a b/c').subscribe();

    expect(http.get).toHaveBeenCalledWith('/backend/registration/public/configuration?invitation=a%20b%2Fc');
    expect(busy.isBusy.next).toHaveBeenCalledWith(false);
  });
});
