import {Router} from "@angular/router";
import {LoginService} from "../services/login.service";
import {MessageService} from "../services/message.service";

export const AUTH_SESSION_ERROR_STATUSES: ReadonlySet<number> = new Set<number>([409, 401, 423]);

export function isAuthSessionErrorStatus(status: number): boolean {
  return AUTH_SESSION_ERROR_STATUSES.has(status);
}

export function logoutAndRedirectToLogin(router: Router, loginService: LoginService, messageService: MessageService): void {
  loginService.logout();
  messageService.warningMessage("userLoggedOutMessage");
  router.navigate(['/login'], {queryParams: {returnUrl: "/tournaments"}});
}
