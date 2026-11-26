import {RxStomp} from '@stomp/rx-stomp';
import {RxStompService} from './rx-stomp.service';

describe('RxStompService', () => {
  it('provides an inactive RxStomp client', () => {
    const client = new RxStompService();
    expect(client).toEqual(jasmine.any(RxStomp));
    expect(client.active).toBeFalse();
  });
});
