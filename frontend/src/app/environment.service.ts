import {Injectable} from '@angular/core';
import {environment} from '../environments/environment';

@Injectable({
  providedIn: 'root'
})
export class EnvironmentService {

  private backendUrl: string = environment.backendUrl;

  constructor() {
  }

  getBackendUrl(): string {
    return this.backendUrl;
  }
}
