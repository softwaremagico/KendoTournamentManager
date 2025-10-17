import {RxStompConfig} from '@stomp/rx-stomp';
import {Environment} from '../../environments/environment';

export const frontendRxStompConfig: RxStompConfig = {
  brokerURL: Environment.websocketsUrl,

  // How often to heartbeat?
  // Interval in milliseconds, set to 0 to disable
  heartbeatIncoming: 0, // Typical value 0 - disabled
  heartbeatOutgoing: 20000, // Typical value 20000 - every 20 seconds

  // Wait in milliseconds before attempting auto reconnect
  // Set to 0 to disable
  // Typical value 500 (500 milliseconds)
  reconnectDelay: 500,

  // Will log diagnostics on console
  // It can be quite verbose, not recommended in production
  // Skip this key to stop logging to console
  debug: (msg: string): void => {
   // console.log(new Date(), msg);
  },

  beforeConnect: (stompClient: any): Promise<void> => {
    return new Promise<void>((resolve, reject): void => {
      const token: string | null = localStorage.getItem('jwt');
      stompClient._stompClient.connectHeaders = {
        "JWT-Token" : token
      };
      resolve();
    });
  }
};
