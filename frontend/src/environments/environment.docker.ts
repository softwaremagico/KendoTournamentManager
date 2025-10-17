export class Environment {
  public static readonly production: true;
  public static readonly backendUrl: string = "DOCKER:BACKEND_PROTOCOL://DOCKER:MACHINE_DOMAIN/kendo-tournament-backend";
  public static readonly websocketsUrl: string = "DOCKER:WEBSOCKET_PROTOCOL://DOCKER:MACHINE_DOMAIN/kendo-tournament-backend/websockets";
  public static readonly websocketsTopicPrefix: string = "/topic";
  public static readonly achievementsEnabled = "DOCKER:ACHIEVEMENTS_ENABLED";
  public static checkForNewVersion = "DOCKER:CHECK_FOR_NEW_VERSION";
}
