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

  setTournament(tournamentId: string) {
    this.cookies.set("lastSelectedTournament", tournamentId);
  }

  getTournament() {
    return this.cookies.get("lastSelectedTournament");
  }

  setItemsPerPage(pageSize: number) {
    if (pageSize) {
      this.cookies.set("itemsPerPage", pageSize.toString());
    }
  }

  getItemsPerPage(): number {
    return Number(this.cookies.get("itemsPerPage"));
  }
}
