import {Injectable} from '@angular/core';
import {environment} from '../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class EnvironmentService {

  private backendUrl: string = environment.backendUrl;
  private websocketUrl: string = environment.websocketsUrl;
  private websocketPrefix: string = environment.websocketsTopicPrefix;

  getBackendUrl(): string {
    return this.backendUrl;
  }

  getWebsocketUrl(): string {
    return this.websocketUrl;
  }

  getWebsocketPrefix(): string {
    return this.websocketPrefix;
  }
}
