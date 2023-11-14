import {Injectable} from '@angular/core';
import {CookieService} from "ngx-cookie-service";

@Injectable({
  providedIn: 'root'
})
export class UserSessionService {

  constructor(private cookies: CookieService) {
  }

  setLanguage(language: string | undefined): void {
    if (language) {
      this.cookies.set("selectedLanguage", language);
    } else {
      this.cookies.delete("selectedLanguage");
    }
  }

  getLanguage(): string {
    return this.cookies.get("selectedLanguage");
  }

  setSelectedTournament(tournamentId: string | undefined): void {
    if (tournamentId) {
      sessionStorage.setItem("lastSelectedTournament", tournamentId);
    } else {
      sessionStorage.removeItem("lastSelectedTournament");
    }
  }

  getSelectedTournament() {
    return sessionStorage.getItem("lastSelectedTournament");
  }

  setSelectedParticipant(participantId: string | undefined): void {
    if (participantId) {
      sessionStorage.setItem("lastSelectedParticipant", participantId);
    } else {
      sessionStorage.removeItem("lastSelectedParticipant");
    }
  }

  getSelectedParticipant() {
    return sessionStorage.getItem("lastSelectedParticipant");
  }

  setItemsPerPage(pageSize: number | undefined): void {
    if (pageSize) {
      sessionStorage.setItem("itemsPerPage", pageSize.toString());
    } else {
      sessionStorage.removeItem("itemsPerPage");
    }
  }

  getItemsPerPage(): number {
    return Number(sessionStorage.getItem("itemsPerPage"));
  }

  setSwappedColors(swappedColors: boolean): void {
    if (swappedColors) {
      sessionStorage.setItem("swappedColors", swappedColors.toString());
    } else {
      sessionStorage.removeItem("swappedColors");
    }
  }

  getSwappedColors(): boolean {
    return Boolean(sessionStorage.getItem("swappedColors"));
  }

  setSwappedTeams(swappedTeams: boolean): void {
    if (swappedTeams) {
      sessionStorage.setItem("swappedTeams", swappedTeams.toString());
    } else {
      sessionStorage.removeItem("swappedTeams");
    }
  }

  getSwappedTeams(): boolean {
    return Boolean(sessionStorage.getItem("swappedTeams"));
  }

  setNightMode(nightMode: boolean): void {
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
