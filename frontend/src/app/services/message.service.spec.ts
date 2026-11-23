import {NEVER, of} from 'rxjs';
import {BiitSnackbarService, NotificationType} from '@biit-solutions/wizardry-theme/info';
import {TranslocoService} from '@jsverse/transloco';
import {MessageService} from './message.service';
import {LoggerService} from './logger.service';
import {RxStompService} from '../websockets/rx-stomp.service';
import {EnvironmentService} from '../environment.service';
import {LoginService} from './login.service';

describe('MessageService', () => {
  let snackBar: jasmine.SpyObj<BiitSnackbarService>;
  let stomp: jasmine.SpyObj<RxStompService>;
  let login: jasmine.SpyObj<LoginService>;
  let translate: jasmine.SpyObj<TranslocoService>;
  let logger: jasmine.SpyObj<LoggerService>;
  let environment: jasmine.SpyObj<EnvironmentService>;
  let service: MessageService;

  beforeEach(() => {
    snackBar = jasmine.createSpyObj('BiitSnackbarService', ['showNotification']);
    translate = jasmine.createSpyObj<TranslocoService>('TranslocoService', ['translate']);
    logger = jasmine.createSpyObj<LoggerService>('LoggerService', ['sendError']);
    stomp = jasmine.createSpyObj<RxStompService>('RxStompService', ['watch'], {connected$: NEVER});
    environment = jasmine.createSpyObj<EnvironmentService>('EnvironmentService', ['getWebsocketPrefix']);
    login = jasmine.createSpyObj<LoginService>('LoginService', ['getTenantId', 'getJwtRoles']);
    (translate.translate.and as any).callFake((message: string) => message);
    environment.getWebsocketPrefix.and.returnValue('/backend');
    login.getTenantId.and.returnValue(1);
    login.getJwtRoles.and.returnValue([]);
    stomp.watch.and.returnValue(NEVER);
    service = new MessageService(snackBar, translate, logger, stomp, environment, login);
  });

  it('shows a repeated error only once during the deduplication window', () => {
    service.errorMessage('The backend is unavailable');
    service.errorMessage('The backend is unavailable');

    expect(snackBar.showNotification).toHaveBeenCalledTimes(1);
    expect(snackBar.showNotification).toHaveBeenCalledWith(
      'The backend is unavailable', NotificationType.ERROR, undefined, 10);
  });

  it('does not suppress distinct errors', () => {
    service.errorMessage('First error');
    service.errorMessage('Second error');

    expect(snackBar.showNotification).toHaveBeenCalledTimes(2);
  });

  it('shows a repeated success message only once during the deduplication window', () => {
    service.infoMessage('Saved successfully');
    service.infoMessage('Saved successfully');

    expect(snackBar.showNotification).toHaveBeenCalledTimes(1);
  });

  it('does not subscribe to administrative messages for read-only sessions', () => {
    const connected = createServiceWithConnection(of(undefined), ['GUEST']);

    expect(connected.stomp.watch).not.toHaveBeenCalled();
  });

  it('subscribes to administrative messages for management roles', () => {
    const connected = createServiceWithConnection(of(undefined), ['VIEWER']);

    expect(connected.stomp.watch).toHaveBeenCalledWith('/backend/tenant/1/messages');
  });

  function createServiceWithConnection(connected$: any, roles: string[]) {
    const localStomp = jasmine.createSpyObj<RxStompService>('RxStompService', ['watch'], {connected$});
    const localLogin = jasmine.createSpyObj<LoginService>('LoginService', ['getTenantId', 'getJwtRoles']);
    localStomp.watch.and.returnValue(NEVER);
    localLogin.getTenantId.and.returnValue(1);
    localLogin.getJwtRoles.and.returnValue(roles);
    const localService = new MessageService(snackBar, translate, logger, localStomp, environment, localLogin);
    return {service: localService, stomp: localStomp};
  }
});
