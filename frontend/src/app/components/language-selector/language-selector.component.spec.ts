import {LanguageSelectorComponent} from './language-selector.component';
import {TranslocoService} from '@ngneat/transloco';
import {UserSessionService} from '../../services/user-session.service';

describe('LanguageSelectorComponent', () => {
  let component: LanguageSelectorComponent;
  let translocoService: jasmine.SpyObj<TranslocoService>;
  let userSessionService: jasmine.SpyObj<UserSessionService>;

  beforeEach(() => {
    translocoService = jasmine.createSpyObj('TranslocoService', [
      'getAvailableLangs',
      'setActiveLang'
    ]);
    userSessionService = jasmine.createSpyObj('UserSessionService', [
      'getLanguage',
      'setLanguage'
    ]);

    translocoService.getAvailableLangs.and.returnValue(['es', 'en', 'fr']);
    userSessionService.getLanguage.and.returnValue('es');

    component = new LanguageSelectorComponent(translocoService, userSessionService);
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should load available languages from TranslocoService on construction', () => {
    expect(translocoService.getAvailableLangs).toHaveBeenCalled();
    expect((component as any).languages).toEqual(['es', 'en', 'fr']);
  });

  it('should load the current language from UserSessionService on construction', () => {
    expect(userSessionService.getLanguage).toHaveBeenCalled();
    expect((component as any).selectedLanguage).toBe('es');
  });

  it('should emit onClosed event when close is called', () => {
    spyOn(component.onClosed, 'emit');

    component.close();

    expect(component.onClosed.emit).toHaveBeenCalledOnceWith();
  });

  it('should switch language by calling both services', () => {
    (component as any).selectedLanguage = 'en';

    component.switchLanguage();

    expect(translocoService.setActiveLang).toHaveBeenCalledOnceWith('en');
    expect(userSessionService.setLanguage).toHaveBeenCalledOnceWith('en');
  });

  it('should handle multiple language switches', () => {
    (component as any).selectedLanguage = 'en';
    component.switchLanguage();

    (component as any).selectedLanguage = 'fr';
    component.switchLanguage();

    expect(translocoService.setActiveLang).toHaveBeenCalledWith('en');
    expect(translocoService.setActiveLang).toHaveBeenCalledWith('fr');
    expect(userSessionService.setLanguage).toHaveBeenCalledWith('en');
    expect(userSessionService.setLanguage).toHaveBeenCalledWith('fr');
  });

  it('should handle empty language list', () => {
    translocoService.getAvailableLangs.and.returnValue([]);

    const newComponent = new LanguageSelectorComponent(
      translocoService,
      userSessionService
    );

    expect((newComponent as any).languages).toEqual([]);
  });

  it('should handle single language', () => {
    translocoService.getAvailableLangs.and.returnValue(['es']);

    const newComponent = new LanguageSelectorComponent(
      translocoService,
      userSessionService
    );

    expect((newComponent as any).languages.length).toBe(1);
    expect((newComponent as any).languages[0]).toBe('es');
  });

  it('should preserve language selection after multiple operations', () => {
    (component as any).selectedLanguage = 'en';
    component.switchLanguage();
    component.close();

    expect((component as any).selectedLanguage).toBe('en');
    expect(translocoService.setActiveLang).toHaveBeenCalledWith('en');
  });
});

