import {Injectable} from '@angular/core';
import {EnvironmentData} from "./environment-data.interface";
import {Environment} from '../environments/environment';

declare const __config: EnvironmentData;

@Injectable({
  providedIn: 'root'
})
export class EnvironmentService {

  private backendUrl: string = __config.backendUrl ? __config.backendUrl : Environment.backendUrl;
  private websocketUrl: string = __config.websocketsUrl ? __config.websocketsUrl : Environment.websocketsUrl;
  private websocketPrefix: string = __config.websocketsTopicPrefix ? __config.websocketsTopicPrefix : Environment.websocketsTopicPrefix;
  private achievementsEnabled: boolean = __config.achievementsEnabled ? __config.achievementsEnabled : (Environment.achievementsEnabled as unknown as boolean);
  private checkForNewVersion: boolean = __config.checkForNewVersion ? __config.checkForNewVersion : (Environment.checkForNewVersion as unknown as boolean);

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
