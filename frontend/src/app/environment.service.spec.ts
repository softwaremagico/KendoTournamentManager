import {Environment} from '../environments/environment';
import {EnvironmentService} from './environment.service';

describe('EnvironmentService', () => {
  let previous: PropertyDescriptor | undefined;

  beforeEach(() => {
    previous = Object.getOwnPropertyDescriptor(globalThis, '__config');
  });

  afterEach(() => {
    if (previous) {
      Object.defineProperty(globalThis, '__config', previous);
    } else {
      delete (globalThis as any).__config;
    }
  });

  it('uses runtime configuration when supplied', () => {
    Object.defineProperty(globalThis, '__config', {configurable: true, value: {
      backendUrl: 'https://example.test/api',
      websocketsUrl: 'wss://example.test/ws',
      websocketsTopicPrefix: '/custom',
      achievementsEnabled: true,
      checkForNewVersion: true
    }});
    const service = new EnvironmentService();
    expect(service.getBackendUrl()).toBe('https://example.test/api');
    expect(service.getWebsocketUrl()).toBe('wss://example.test/ws');
    expect(service.getWebsocketPrefix()).toBe('/custom');
    expect(service.isAchievementsEnabled()).toBeTrue();
    expect(service.isCheckForNewVersion()).toBeTrue();
  });

  it('uses build defaults when runtime values are absent', () => {
    Object.defineProperty(globalThis, '__config', {configurable: true, value: {}});
    const service = new EnvironmentService();
    expect(service.getBackendUrl()).toBe(Environment.backendUrl);
    expect(service.getWebsocketUrl()).toBe(Environment.websocketsUrl);
    expect(service.getWebsocketPrefix()).toBe(Environment.websocketsTopicPrefix);
  });
});
