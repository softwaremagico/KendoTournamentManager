import {RxStompService} from './rx-stomp.service';
import {frontendRxStompConfig} from './frontend-rx-stomp.config';

export function rxStompServiceFactory(): RxStompService {
  const rxStomp: RxStompService = new RxStompService();
  rxStomp.configure(frontendRxStompConfig);
  rxStomp.activate();
  return rxStomp;
}
