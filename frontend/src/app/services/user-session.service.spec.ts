import {CookieService} from 'ngx-cookie-service';
import {UserSessionService} from './user-session.service';

describe('UserSessionService', () => {
  let cookies: jasmine.SpyObj<CookieService>;
  let service: UserSessionService;
  let localBefore: Record<string, string>;
  let sessionBefore: Record<string, string>;

  function snapshot(storage: Storage): Record<string, string> {
    const result: Record<string, string> = {};
    for (let i = 0; i < storage.length; i++) {
      const key = storage.key(i)!;
      result[key] = storage.getItem(key)!;
    }
    return result;
  }

  function restore(storage: Storage, values: Record<string, string>): void {
    storage.clear();
    Object.entries(values).forEach(([key, value]) => storage.setItem(key, value));
  }

  beforeEach(() => {
    localBefore = snapshot(localStorage);
    sessionBefore = snapshot(sessionStorage);
    localStorage.clear();
    sessionStorage.clear();
    cookies = jasmine.createSpyObj<CookieService>('CookieService', ['get', 'set', 'delete']);
    service = new UserSessionService(cookies);
  });

  afterEach(() => {
    restore(localStorage, localBefore);
    restore(sessionStorage, sessionBefore);
  });

  it('stores and clears the selected language', () => {
    service.setLanguage('es');
    expect(cookies.set).toHaveBeenCalledWith('selectedLanguage', 'es');
    cookies.get.and.returnValue('es');
    expect(service.getLanguage()).toBe('es');
    service.setLanguage(undefined);
    expect(cookies.delete).toHaveBeenCalledWith('selectedLanguage');
  });

  it('stores and clears selected tournament and participant', () => {
    service.setSelectedTournament('12');
    expect(sessionStorage.getItem('lastSelectedTournament')).toBe('12');
    expect(localStorage.getItem('tournamentId')).toBe('12');
    service.setSelectedParticipant('8');
    expect(service.getSelectedParticipant()).toBe('8');
    service.setSelectedTournament(undefined);
    service.setSelectedParticipant(undefined);
    expect(localStorage.getItem('tournamentId')).toBeNull();
    expect(service.getSelectedParticipant()).toBeNull();
  });

  it('persists and clears display preferences', () => {
    service.setItemsPerPage(25);
    service.enableSwappedColors();
    service.enableSwappedTeams();
    service.enableNightMode();
    expect(service.getItemsPerPage()).toBe(25);
    expect(service.getSwappedColors()).toBeTrue();
    expect(service.getSwappedTeams()).toBeTrue();
    expect(service.getNightMode()).toBeTrue();
    service.setItemsPerPage(undefined);
    service.disableSwappedColors();
    service.disableSwappedTeams();
    service.disableNightMode();
    expect(service.getItemsPerPage()).toBe(0);
    expect(service.getSwappedColors()).toBeFalse();
    expect(service.getSwappedTeams()).toBeFalse();
    expect(service.getNightMode()).toBeFalse();
  });

  it('clears credentials on logout', () => {
    service.setAuthToken('signed-token');
    service.setExpirationDate(Date.now() + 60_000);
    expect(service.getAuthToken()).toBe('signed-token');
    expect(service.isLoggedIn).toBeTrue();
    service.clearToken();
    expect(service.getAuthToken()).toBeNull();
    expect(service.getLocalAuthExpiration()).toBeNull();
    expect(service.isLoggedIn).toBeFalse();
  });

  it('removes expired stored credentials on startup', () => {
    localStorage.setItem('.jwt', 'old-token');
    localStorage.setItem('.jwt_expires', '1');
    new UserSessionService(cookies);
    expect(localStorage.getItem('.jwt')).toBeNull();
    expect(localStorage.getItem('.jwt_expires')).toBeNull();
  });
});
