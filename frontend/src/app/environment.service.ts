import {Injectable} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class EnvironmentService {

  private backendUrl: string = "http://localhost:8080/kendo-tournament-backend";

  constructor() {
  }

  getBackendUrl(): string {
    return this.backendUrl;
  }
}
