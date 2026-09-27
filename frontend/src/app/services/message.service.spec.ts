import {NEVER} from 'rxjs';
import {BiitSnackbarService, NotificationType} from '@biit-solutions/wizardry-theme/info';
import {TranslocoService} from '@jsverse/transloco';
import {MessageService} from './message.service';
import {LoggerService} from './logger.service';
import {RxStompService} from '../websockets/rx-stomp.service';
import {EnvironmentService} from '../environment.service';
import {LoginService} from './login.service';

describe('MessageService', () => {
  let snackBar: jasmine.SpyObj<BiitSnackbarService>;
  let service: MessageService;

  beforeEach(() => {
    snackBar = jasmine.createSpyObj('BiitSnackbarService', ['showNotification']);
    const translate = jasmine.createSpyObj<TranslocoService>('TranslocoService', ['translate']);
    const logger = jasmine.createSpyObj<LoggerService>('LoggerService', ['sendError']);
    const stomp = jasmine.createSpyObj<RxStompService>('RxStompService', ['watch']);
    const environment = jasmine.createSpyObj<EnvironmentService>('EnvironmentService', ['getWebsocketPrefix']);
    const login = jasmine.createSpyObj<LoginService>('LoginService', ['getTenantId']);
    (translate.translate.and as any).callFake((message: string) => message);
    environment.getWebsocketPrefix.and.returnValue('/backend');
    login.getTenantId.and.returnValue(1);
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
});
