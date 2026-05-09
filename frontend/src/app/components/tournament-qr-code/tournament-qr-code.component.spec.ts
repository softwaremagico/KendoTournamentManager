import { of } from 'rxjs';
import { QrCode } from '../../models/qr-code.model';
import { Tournament } from '../../models/tournament';
import { QrService } from '../../services/qr.service';
import { RbacService } from '../../services/rbac/rbac.service';
import { UserSessionService } from '../../services/user-session.service';
import { TournamentQrCodeComponent } from './tournament-qr-code.component';

describe('TournamentQrCodeComponent', () => {
  let component: TournamentQrCodeComponent;
  let qrServiceSpy: jasmine.SpyObj<QrService>;
  let userSessionServiceSpy: jasmine.SpyObj<UserSessionService>;
  let rbacServiceSpy: jasmine.SpyObj<RbacService>;

  const createTournament = (id: number, name: string): Tournament => ({
    id,
    name,
    teamSize: 3,
    fightSize: 3,
    duelsDuration: 180,
    locked: false
  } as unknown as Tournament);

  beforeEach(() => {
    qrServiceSpy = jasmine.createSpyObj('QrService', ['getGuestsQr', 'getGuestsQrAsPdf']);
    userSessionServiceSpy = jasmine.createSpyObj('UserSessionService', ['getNightMode']);
    rbacServiceSpy = jasmine.createSpyObj('RbacService', ['isAllowed']);

    component = new TournamentQrCodeComponent(
      qrServiceSpy,
      userSessionServiceSpy,
      rbacServiceSpy
    );
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should load tournament qr code and link on init when tournament exists', () => {
    const tournament = createTournament(11, 'Open Cup');
    const response = {
      base64: 'data:image/png;base64,xyz',
      content: 'https://example.org/t/11'
    } as unknown as QrCode;

    component.tournament = tournament;
    component.port = 9090;
    userSessionServiceSpy.getNightMode.and.returnValue(false);
    qrServiceSpy.getGuestsQr.and.returnValue(of(response));

    component.ngOnInit();

    expect(userSessionServiceSpy.getNightMode).toHaveBeenCalled();
    expect(qrServiceSpy.getGuestsQr).toHaveBeenCalledOnceWith(tournament, false, 9090);
    expect(component.qrCode).toBe('data:image/png;base64,xyz');
    expect(component.link).toBe('https://example.org/t/11');
  });

  it('should set qrCode to undefined when qr response is empty', () => {
    component.tournament = createTournament(5, 'Regional');
    userSessionServiceSpy.getNightMode.and.returnValue(true);
    qrServiceSpy.getGuestsQr.and.returnValue(of(null as unknown as QrCode));

    component.ngOnInit();

    expect(component.qrCode).toBeUndefined();
  });

  it('should not call qr service on init when tournament is undefined', () => {
    component.tournament = undefined as unknown as Tournament;

    component.ngOnInit();

    expect(qrServiceSpy.getGuestsQr).not.toHaveBeenCalled();
  });

  it('should emit onClosed when closeDialog is called', () => {
    spyOn(component.closed, 'emit');

    component.closeDialog();

    expect(component.closed.emit).toHaveBeenCalledOnceWith();
  });

  it('should request pdf and trigger browser download when blob is returned', () => {
    const tournament = createTournament(8, 'National');
    const blob = new Blob(['pdf-content'], { type: 'application/pdf' });
    const anchorMock = {
      download: '',
      href: '',
      click: jasmine.createSpy('click')
    } as unknown as HTMLAnchorElement;

    component.tournament = tournament;
    qrServiceSpy.getGuestsQrAsPdf.and.returnValue(of(blob));
    spyOn(globalThis.URL, 'createObjectURL').and.returnValue('blob:mock-url');
    spyOn(document, 'createElement').and.returnValue(anchorMock as any);

    component.downloadQrAsPdf();

    expect(qrServiceSpy.getGuestsQrAsPdf).toHaveBeenCalledOnceWith(tournament);
    expect(globalThis.URL.createObjectURL).toHaveBeenCalled();
    expect(anchorMock.href).toBe('blob:mock-url');
    expect(anchorMock.download).toContain('Tournament -');
    expect((anchorMock.click as jasmine.Spy)).toHaveBeenCalled();
  });

  it('should not download when tournament is undefined', () => {
    component.tournament = undefined as unknown as Tournament;

    component.downloadQrAsPdf();

    expect(qrServiceSpy.getGuestsQrAsPdf).not.toHaveBeenCalled();
  });
});

