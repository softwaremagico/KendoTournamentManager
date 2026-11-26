import {Router} from '@angular/router';
import {LoginService} from '../services/login.service';
import {MessageService} from '../services/message.service';
import {logoutAndRedirectToLogin} from './auth-session-error';

describe('auth-session-error', () => {
  it('cierra la sesión, avisa y redirige al login con returnUrl', () => {
    const router = jasmine.createSpyObj<Router>('Router', ['navigate']);
    const loginService = jasmine.createSpyObj<LoginService>('LoginService', ['logout']);
    const messageService = jasmine.createSpyObj<MessageService>('MessageService', ['warningMessage']);

    logoutAndRedirectToLogin(router, loginService, messageService);

    expect(loginService.logout).toHaveBeenCalled();
    expect(messageService.warningMessage).toHaveBeenCalledWith('userLoggedOutMessage');
    expect(router.navigate).toHaveBeenCalledWith(['/login'], {queryParams: {returnUrl: '/tournaments'}});
  });
});
