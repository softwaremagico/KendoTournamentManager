import {Injectable} from "@angular/core";

@Injectable({
  providedIn: 'root'
})
export class TokenService {

  public getId(token: string): string {
    return this.getPayload(token).split(',')[0];
  }

  public getUsername(token: string): string {
    return this.getPayload(token).split(',')[1];
  }

  public getSession(token: string): string {
    return this.getPayload(token).split(',')[2];
  }

  public getIp(token: string): string {
    return this.getPayload(token).split(',')[3];
  }

  public getMac(token: string): string {
    return this.getPayload(token).split(',')[4];
  }

  private getPayload(token: string): string {
    return JSON.parse(window.atob(token.split('.')[1]));
  }
}
