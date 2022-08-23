import {Injectable} from '@angular/core';
import {CookieService} from "ngx-cookie-service";

@Injectable({
  providedIn: 'root'
})
export class UserSessionService {

  constructor(private cookies: CookieService) {
  }

  setLanguage(language: string | undefined) {
    if (language) {
      this.cookies.set("selectedLanguage", language);
    } else {
      this.cookies.delete("selectedLanguage");
    }
  }

  getLanguage(): string {
    return this.cookies.get("selectedLanguage");
  }

  setTournament(tournamentId: string | undefined) {
    if (tournamentId) {
      this.cookies.set("lastSelectedTournament", tournamentId);
    } else {
      this.cookies.delete("lastSelectedTournament");
    }
  }

  getTournament() {
    return this.cookies.get("lastSelectedTournament");
  }

  setItemsPerPage(pageSize: number | undefined) {
    if (pageSize) {
      this.cookies.set("itemsPerPage", pageSize.toString());
    } else {
      this.cookies.delete("itemsPerPage");
    }
  }

  getItemsPerPage(): number {
    return Number(this.cookies.get("itemsPerPage"));
  }

  setSwappedColors(swappedColors: boolean) {
    if (swappedColors) {
      this.cookies.set("swappedColors", swappedColors.toString());
    } else {
      this.cookies.delete("swappedColors");
    }
  }

  getSwappedColors(): boolean {
    return Boolean(this.cookies.get("swappedColors"));
  }
}
