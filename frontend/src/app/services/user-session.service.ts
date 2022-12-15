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
      sessionStorage.setItem("lastSelectedTournament", tournamentId);
    } else {
      sessionStorage.removeItem("lastSelectedTournament");
    }
  }

  getTournament() {
    return sessionStorage.getItem("lastSelectedTournament");
  }

  setItemsPerPage(pageSize: number | undefined) {
    if (pageSize) {
      sessionStorage.setItem("itemsPerPage", pageSize.toString());
    } else {
      sessionStorage.removeItem("itemsPerPage");
    }
  }

  getItemsPerPage(): number {
    return Number(sessionStorage.getItem("itemsPerPage"));
  }

  setSwappedColors(swappedColors: boolean) {
    if (swappedColors) {
      sessionStorage.setItem("swappedColors", swappedColors.toString());
    } else {
      sessionStorage.removeItem("swappedColors");
    }
  }

  getSwappedColors(): boolean {
    return Boolean(sessionStorage.getItem("swappedColors"));
  }

  setSwappedTeams(swappedTeams: boolean) {
    if (swappedTeams) {
      sessionStorage.setItem("swappedTeams", swappedTeams.toString());
    } else {
      sessionStorage.removeItem("swappedTeams");
    }
  }

  getSwappedTeams(): boolean {
    return Boolean(sessionStorage.getItem("swappedTeams"));
  }

  setNightMode(nightMode: boolean) {
    if (nightMode) {
      sessionStorage.setItem("nightMode", nightMode.toString());
    } else {
      sessionStorage.removeItem("nightMode");
    }
  }

  getNightMode(): boolean {
    return Boolean(sessionStorage.getItem("nightMode"));
  }
}
