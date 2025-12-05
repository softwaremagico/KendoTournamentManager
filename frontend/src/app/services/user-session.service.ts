import {Injectable} from '@angular/core';
import {CookieService} from "ngx-cookie-service";
import {Constants} from "../constants";
import {AuthenticatedUser} from "../models/authenticated-user";

@Injectable({
  providedIn: 'root'
})
export class UserSessionService {
  private loggedIn: boolean = false;
  private user: AuthenticatedUser | undefined;
  private store: boolean;
  private readonly context: string = '';

  constructor(private cookies: CookieService) {
    const authToken: string | null = this.getAuthToken();
    const expires: number | null = this.getLocalAuthExpiration();

    const localUserData: string | null = this.getLocalUser();
    let user: AuthenticatedUser | undefined = localUserData != null ? AuthenticatedUser.clone(
      JSON.parse(localUserData)) : undefined;
    if (!user) {
      const sessionUserData: string | null = this.getSessionUser();
      try {
        user = sessionUserData != null || sessionUserData != undefined ? AuthenticatedUser.clone(JSON.parse(sessionUserData)) : undefined;
      } catch (e) {

      }
    }
    this.user = user;
    if (!expires || isNaN(expires) || expires < new Date().getTime()) {
      localStorage.removeItem(`${this.context}.${Constants.SESSION_STORAGE.AUTH_TOKEN}`);
      localStorage.removeItem(`${this.context}.${Constants.SESSION_STORAGE.AUTH_EXPIRATION}`);
      localStorage.removeItem(`${this.context}.${Constants.SESSION_STORAGE.USER}`);
    }
    if (authToken) {
      sessionStorage.setItem(`${this.context}.${Constants.SESSION_STORAGE.AUTH_TOKEN}`, authToken);
      this.setSessionUser(user);
      this.store = true;
      if (expires && !isNaN(expires)) {
        sessionStorage.setItem(`${this.context}.${Constants.SESSION_STORAGE.AUTH_EXPIRATION}`, expires.toString());
      }
      this.loggedIn = true;
    }
    if ((authToken && expires)
      || (sessionStorage.getItem(`${this.context}.${Constants.SESSION_STORAGE.AUTH_TOKEN}`) && sessionStorage.getItem(`${this.context}.${Constants.SESSION_STORAGE.AUTH_EXPIRATION}`))) {
      this.setAutoRenew(authToken, expires);
    }
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

  getSelectedParticipant(): string | null {
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
      localStorage.setItem("nightMode", nightMode.toString());
    } else {
      localStorage.removeItem("nightMode");
    }
  }

  getNightMode(): boolean {
    return Boolean(localStorage.getItem("nightMode"));
  }

  setAuthToken(authToken: string): void {
    localStorage.setItem(`${this.context}.${Constants.SESSION_STORAGE.AUTH_TOKEN}`, authToken);
    this.loggedIn = true;
  }

  getAuthToken(): string | null {
    return localStorage.getItem(`${this.context}.${Constants.SESSION_STORAGE.AUTH_TOKEN}`);
  }

  getLocalAuthExpiration(): number | null {
    const data: string | null = localStorage.getItem(`${this.context}.${Constants.SESSION_STORAGE.AUTH_EXPIRATION}`);
    if (data) {
      return +data;
    }
    return null;
  }

  getSessionAuthExpiration(): number | null {
    const data: string | null = sessionStorage.getItem(`${this.context}.${Constants.SESSION_STORAGE.AUTH_EXPIRATION}`);
    if (data) {
      return +data;
    }
    return null;
  }

  getLocalUser(): string | null {
    return localStorage.getItem(`${this.context}.${Constants.SESSION_STORAGE.USER}`);
  }

  setSessionUser(user: AuthenticatedUser | undefined) {
    sessionStorage.setItem(`${this.context}.${Constants.SESSION_STORAGE.USER}`, JSON.stringify(user));
  }

  getSessionUser(): string | null {
    return sessionStorage.getItem(`${this.context}.${Constants.SESSION_STORAGE.USER}`)
  }

  get isLoggedIn(): boolean {
    return this.loggedIn;
  }

  isTokenExpired(): boolean {
    const expired: boolean = !sessionStorage.getItem(`${this.context}.${Constants.SESSION_STORAGE.AUTH_EXPIRATION}`) ||
      new Date().getTime() > +(this.getLocalAuthExpiration() || 0) || !this.getToken();
    if (!expired) {
      this.loggedIn = true;
    }
    return expired;
  }

  getExpirationDate(): Date | null {
    const sessionExpiration: string | null = sessionStorage.getItem(`${this.context}.${Constants.SESSION_STORAGE.AUTH_EXPIRATION}`);
    if (!isNaN(+(sessionExpiration || NaN))) {
      return new Date(+(this.getSessionAuthExpiration() || 0));
    }
    return null;
  }

  setExpirationDate(expires: number) {
    localStorage.setItem(`${this.context}.${Constants.SESSION_STORAGE.AUTH_EXPIRATION}`, expires.toString());
  }

  getToken(): string | null {
    return sessionStorage.getItem(`${Constants.SESSION_STORAGE.AUTH_TOKEN}`);
  }

  private setAutoRenew(token: string | null, expires: number | null): void {
    if (token && expires) {
    }
  }

  clearToken(): void {
    sessionStorage.removeItem(`${this.context}.${Constants.SESSION_STORAGE.AUTH_TOKEN}`);
    sessionStorage.removeItem(`${this.context}.${Constants.SESSION_STORAGE.AUTH_EXPIRATION}`);
    sessionStorage.removeItem(`${this.context}.${Constants.SESSION_STORAGE.USER}`);
    this.store = false;
    localStorage.removeItem(`${this.context}.${Constants.SESSION_STORAGE.AUTH_TOKEN}`);
    localStorage.removeItem(`${this.context}.${Constants.SESSION_STORAGE.AUTH_EXPIRATION}`);
    localStorage.removeItem(`${this.context}.${Constants.SESSION_STORAGE.USER}`);
    this.loggedIn = false;
    this.user = undefined;
  }

  setUser(user: AuthenticatedUser, enableStore: boolean | undefined = undefined): void {
    sessionStorage.setItem(`${this.context}.${Constants.SESSION_STORAGE.USER}`, JSON.stringify(user));
    if (enableStore !== undefined) {
      if (!enableStore) {
        localStorage.removeItem(`${this.context}.${Constants.SESSION_STORAGE.USER}`);
      }
    }
    if (this.store) {
      localStorage.setItem(`${this.context}.${Constants.SESSION_STORAGE.USER}`, JSON.stringify(user));
    }
    this.user = user;
  }

  getUser(): AuthenticatedUser | undefined {
    return this.user;
  }
}
