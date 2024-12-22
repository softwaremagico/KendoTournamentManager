export const environment = {
  production: true,
  backendUrl: "DOCKER:BACKEND_PROTOCOL://DOCKER:MACHINE_DOMAIN/kendo-tournament-backend",
  websocketsUrl: "DOCKER:WEBSOCKET_PROTOCOL://DOCKER:MACHINE_DOMAIN/kendo-tournament-backend/websockets",
  websocketsTopicPrefix: "/topic",
  achievementsEnabled: "DOCKER:ACHIEVEMENTS_ENABLED",
};
