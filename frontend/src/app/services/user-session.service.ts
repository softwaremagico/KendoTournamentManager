import {Injectable} from '@angular/core';
import {CookieService} from "ngx-cookie-service";

@Injectable({
  providedIn: 'root'
})
export class UserSessionService {

  constructor(private cookies: CookieService) {
  }

  setLanguage(language: string) {
    this.cookies.set("selectedLanguage", language);
  }

  getLanguage(): string {
    return this.cookies.get("selectedLanguage");
  }
}
