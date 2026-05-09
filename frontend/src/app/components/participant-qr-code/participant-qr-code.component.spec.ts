import {of} from 'rxjs';
import {Participant} from '../../models/participant';
import {QrCode} from '../../models/qr-code.model';
import {QrService} from '../../services/qr.service';
import {RbacService} from '../../services/rbac/rbac.service';
import {UserSessionService} from '../../services/user-session.service';
import {ParticipantQrCodeComponent} from './participant-qr-code.component';

describe('ParticipantQrCodeComponent', () => {
  let component: ParticipantQrCodeComponent;
  let qrServiceSpy: jasmine.SpyObj<QrService>;
  let userSessionServiceSpy: jasmine.SpyObj<UserSessionService>;
  let rbacServiceSpy: jasmine.SpyObj<RbacService>;

  const createParticipant = (id: number): Participant => ({
    id,
    name: 'John',
    lastname: 'Doe',
    idCard: `ID${id}`,
    hasAvatar: false,
    locked: false
  } as unknown as Participant);

  beforeEach(() => {
    qrServiceSpy = jasmine.createSpyObj('QrService', ['getParticipantQr']);
    userSessionServiceSpy = jasmine.createSpyObj('UserSessionService', ['getNightMode']);
    rbacServiceSpy = jasmine.createSpyObj('RbacService', ['isAllowed']);

    component = new ParticipantQrCodeComponent(
      qrServiceSpy,
      userSessionServiceSpy,
      rbacServiceSpy
    );
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should load participant qr code and link on init when participant exists', () => {
    const participant = createParticipant(7);
    const response = {
      base64: 'data:image/png;base64,abc',
      content: 'https://example.org/p/7'
    } as unknown as QrCode;

    component.participant = participant;
    component.port = 8080;
    userSessionServiceSpy.getNightMode.and.returnValue(true);
    qrServiceSpy.getParticipantQr.and.returnValue(of(response));

    component.ngOnInit();

    expect(userSessionServiceSpy.getNightMode).toHaveBeenCalled();
    expect(qrServiceSpy.getParticipantQr).toHaveBeenCalledOnceWith(7, true, 8080);
    expect((component as any).qrCode).toBe('data:image/png;base64,abc');
    expect((component as any).link).toBe('https://example.org/p/7');
  });

  it('should set qrCode to undefined when service returns empty response', () => {
    const participant = createParticipant(3);

    component.participant = participant;
    userSessionServiceSpy.getNightMode.and.returnValue(false);
    qrServiceSpy.getParticipantQr.and.returnValue(of(null as unknown as QrCode));

    component.ngOnInit();

    expect((component as any).qrCode).toBeUndefined();
  });

  it('should not call qr service on init when participant is undefined', () => {
    component.participant = undefined as unknown as Participant;

    component.ngOnInit();

    expect(qrServiceSpy.getParticipantQr).not.toHaveBeenCalled();
  });

  it('should emit onClosed when close is called', () => {
    spyOn(component.closed, 'emit');

    component.close();

    expect(component.closed.emit).toHaveBeenCalledOnceWith();
  });
});

