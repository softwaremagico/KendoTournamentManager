// This file can be replaced during build by using the `fileReplacements` array.
// `ng build` replaces `environment.ts` with `environment.prod.ts`.
// The list of file replacements can be found in `angular.json`.

export const environment = {
  production: false,
  backendUrl: "http://localhost:8080/kendo-tournament-backend",
  websocketsUrl: "ws://127.0.0.1:8080/kendo-tournament-backend/websockets",
  websocketsTopicPrefix: "/topic"
};
