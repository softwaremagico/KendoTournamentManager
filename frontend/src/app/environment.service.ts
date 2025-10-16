import {Injectable} from '@angular/core';
import {Environment} from "./environment.interface";
import {environment} from '../environments/environment';

declare let __config: Environment;

@Injectable({
  providedIn: 'root'
})
export class EnvironmentService {

  private backendUrl: string = __config.backendUrl ? __config.backendUrl : environment.backendUrl;
  private websocketUrl: string = __config.websocketsUrl ? __config.websocketsUrl : environment.websocketsUrl;
  private websocketPrefix: string = __config.websocketsTopicPrefix ? __config.websocketsUrl : environment.websocketsUrl;
  private achievementsEnabled: boolean = __config.achievementsEnabled ? __config.achievementsEnabled : environment.achievementsEnabled;
  private checkForNewVersion: boolean = __config.checkForNewVersion ? __config.checkForNewVersion : environment.checkForNewVersion;

  getBackendUrl(): string {
    return this.backendUrl;
  }

  getWebsocketUrl(): string {
    return this.websocketUrl;
  }

  getWebsocketPrefix(): string {
    return this.websocketPrefix;
  }

  isAchievementsEnabled(): boolean {
    return this.achievementsEnabled;
  }

  isCheckForNewVersion(): boolean {
    return this.checkForNewVersion;
  }
}
