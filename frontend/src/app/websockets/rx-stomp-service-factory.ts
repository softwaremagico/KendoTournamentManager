import {RxStompService} from './rx-stomp.service';
import {frontendRxStompConfig} from './frontend-rx-stomp.config';
import {UserSessionService} from '../services/user-session.service';

export function rxStompServiceFactory(sessionService: UserSessionService): RxStompService {
  const rxStomp: RxStompService = new RxStompService();
  rxStomp.configure(frontendRxStompConfig);
  if (sessionService.getAuthToken()) {
    rxStomp.activate();
  }
  return rxStomp;
}
