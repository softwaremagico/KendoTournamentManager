export class Environment {
  public static readonly production: true;
  public static readonly backendUrl: string = "http://localhost:8080/kendo-tournament-backend";
  public static readonly websocketsUrl: string = "http://localhost:8080/kendo-tournament-backend/websockets";
  public static readonly websocketsTopicPrefix: string = "/topic";
  public static readonly achievementsEnabled: true;
  public static readonly checkForNewVersion: true;
};
