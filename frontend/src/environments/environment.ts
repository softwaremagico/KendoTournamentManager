export class Environment {
  public static readonly production: false;
  public static readonly backendUrl: string = "http://localhost:8080/kendo-tournament-backend";
  public static readonly websocketsUrl: string = "ws://localhost:8080/kendo-tournament-backend/websockets";
  public static readonly websocketsTopicPrefix: string = "/topic";
  public static readonly achievementsEnabled: true;
  public static readonly checkForNewVersion: false;
}
