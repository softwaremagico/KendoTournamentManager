import {of} from 'rxjs';
import {BiitSnackbarService, NotificationType} from '@biit-solutions/wizardry-theme/info';
import {ClubFormPopupComponent} from './club-form-popup.component';
import {UserSessionService} from '../../../services/user-session.service';
import {CsvService} from '../../../services/csv-service';
import {TranslocoService} from '@ngneat/transloco';

describe('ClubFormPopupComponent', () => {
  let component: ClubFormPopupComponent;
  let sessionServiceSpy: jasmine.SpyObj<UserSessionService>;
  let csvServiceSpy: jasmine.SpyObj<CsvService>;
  let biitSnackbarServiceSpy: jasmine.SpyObj<BiitSnackbarService>;
  let translocoServiceSpy: jasmine.SpyObj<TranslocoService>;

  beforeEach(() => {
    sessionServiceSpy = jasmine.createSpyObj('UserSessionService', ['getUser']);
    csvServiceSpy = jasmine.createSpyObj('CsvService', ['addClubs']);
    biitSnackbarServiceSpy = jasmine.createSpyObj('BiitSnackbarService', ['showNotification']);
    translocoServiceSpy = jasmine.createSpyObj('TranslocoService', ['selectTranslate']);

    sessionServiceSpy.getUser.and.returnValue({ username: 'admin' } as any);
    translocoServiceSpy.selectTranslate.and.returnValue(of('translated message'));

    component = new ClubFormPopupComponent(
      sessionServiceSpy,
      csvServiceSpy,
      biitSnackbarServiceSpy,
      translocoServiceSpy
    );
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should load logged user on ngOnInit', () => {
    component.ngOnInit();

    expect(sessionServiceSpy.getUser).toHaveBeenCalled();
    expect((component as any).loggedUser).toEqual({ username: 'admin' } as any);
  });

  it('should import clubs, show success notification and emit onSaved when csv has no errors', () => {
    csvServiceSpy.addClubs.and.returnValue(of([]));
    spyOn(component.saved, 'emit');

    const file = new File(['name,country'], 'clubs.csv', { type: 'text/csv' });
    const input = document.createElement('input');
    const fileList = {
      length: 1,
      item: (_index: number) => file
    } as unknown as FileList;

    Object.defineProperty(input, 'files', {
      configurable: true,
      get: () => fileList
    });

    component.handleFileInput({ currentTarget: input } as unknown as Event);

    expect(csvServiceSpy.addClubs).toHaveBeenCalledOnceWith(file);
    expect(translocoServiceSpy.selectTranslate).toHaveBeenCalledWith('clubStored');
    expect(biitSnackbarServiceSpy.showNotification).toHaveBeenCalledWith('translated message', NotificationType.SUCCESS);
    expect(component.saved.emit).toHaveBeenCalled();
  });

  it('should show error notification when csv import returns failing clubs', () => {
    csvServiceSpy.addClubs.and.returnValue(of([{ name: 'Duplicated Club' }] as any));

    const file = new File(['name,country'], 'clubs.csv', { type: 'text/csv' });
    const input = document.createElement('input');
    const fileList = {
      length: 1,
      item: (_index: number) => file
    } as unknown as FileList;

    Object.defineProperty(input, 'files', {
      configurable: true,
      get: () => fileList
    });

    component.handleFileInput({ currentTarget: input } as unknown as Event);

    expect(translocoServiceSpy.selectTranslate).toHaveBeenCalledWith(
      'failedOnCsvField',
      { element: 'Duplicated Club' }
    );
    expect(biitSnackbarServiceSpy.showNotification).toHaveBeenCalledWith('translated message', NotificationType.ERROR);
  });

  it('should not call csv service when no file is provided', () => {
    const input = document.createElement('input');
    Object.defineProperty(input, 'files', {
      configurable: true,
      get: () => null
    });

    component.handleFileInput({ currentTarget: input } as unknown as Event);

    expect(csvServiceSpy.addClubs).not.toHaveBeenCalled();
  });
});

