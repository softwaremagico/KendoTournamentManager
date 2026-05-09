import { BehaviorSubject, of } from 'rxjs';
import { NotificationType } from '@biit-solutions/wizardry-theme/info';
import { ParticipantFormComponent } from './participant-form.component';
import { RbacService } from '../../services/rbac/rbac.service';
import { TranslocoService } from '@ngneat/transloco';
import { BiitSnackbarService } from '@biit-solutions/wizardry-theme/info';
import { ParticipantService } from '../../services/participant.service';
import { ClubService } from '../../services/club.service';
import { PictureUpdatedService } from '../../services/notifications/picture-updated.service';
import { FileService } from '../../services/file.service';
import { MessageService } from '../../services/message.service';
import { Participant } from '../../models/participant';
import { Club } from '../../models/club';
import { ParticipantFormValidationFields } from './participant-form-validation-fields';

describe('ParticipantFormComponent', () => {
  let component: ParticipantFormComponent;
  let rbacServiceSpy: jasmine.SpyObj<RbacService>;
  let translocoServiceSpy: jasmine.SpyObj<TranslocoService>;
  let biitSnackbarServiceSpy: jasmine.SpyObj<BiitSnackbarService>;
  let participantServiceSpy: jasmine.SpyObj<ParticipantService>;
  let clubServiceSpy: jasmine.SpyObj<ClubService>;
  let fileServiceSpy: jasmine.SpyObj<FileService>;
  let messageServiceSpy: jasmine.SpyObj<MessageService>;
  let pictureUpdatedServiceMock: PictureUpdatedService;

  const club1: Club = { id: 1, name: 'Club A', country: 'ES', city: 'Madrid' } as Club;
  const club2: Club = { id: 2, name: 'Club B', country: 'IT', city: 'Rome' } as Club;

  const buildParticipant = (id?: number): Participant => ({
    id,
    name: 'John',
    lastname: 'Doe',
    idCard: 'ABC123',
    club: club1
  } as Participant);

  beforeEach(() => {
    rbacServiceSpy = jasmine.createSpyObj('RbacService', ['isAllowed']);
    translocoServiceSpy = jasmine.createSpyObj('TranslocoService', ['translate']);
    biitSnackbarServiceSpy = jasmine.createSpyObj('BiitSnackbarService', ['showNotification']);
    participantServiceSpy = jasmine.createSpyObj('ParticipantService', ['add', 'update']);
    clubServiceSpy = jasmine.createSpyObj('ClubService', ['getAll']);
    fileServiceSpy = jasmine.createSpyObj('FileService', ['getParticipantPicture', 'deleteParticipantPicture']);
    messageServiceSpy = jasmine.createSpyObj('MessageService', ['infoMessage']);

    pictureUpdatedServiceMock = {
      isPictureUpdated: new BehaviorSubject<string>('')
    } as PictureUpdatedService;

    rbacServiceSpy.isAllowed.and.returnValue(true);
    translocoServiceSpy.translate.and.returnValue('validation error');
    clubServiceSpy.getAll.and.returnValue(of([club1, club2]));
    fileServiceSpy.getParticipantPicture.and.returnValue(of(null) as any);
    fileServiceSpy.deleteParticipantPicture.and.returnValue(of({}) as any);

    component = new ParticipantFormComponent(
      rbacServiceSpy,
      translocoServiceSpy,
      biitSnackbarServiceSpy,
      participantServiceSpy,
      clubServiceSpy,
      pictureUpdatedServiceMock,
      fileServiceSpy,
      messageServiceSpy
    );

    component.participant = buildParticipant(1);
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should load clubs in constructor and build translatedClubs', () => {
    expect(clubServiceSpy.getAll).toHaveBeenCalled();
    expect((component as any).clubs.length).toBe(2);
    expect((component as any).translatedClubs.length).toBe(2);
  });

  it('should initialize participant picture from pictureUpdated stream', () => {
    component.ngOnInit();
    pictureUpdatedServiceMock.isPictureUpdated.next('base64-updated');

    expect(component.participantPicture).toBe('base64-updated');
  });

  it('should load participant picture when participant has id', () => {
    fileServiceSpy.getParticipantPicture.and.returnValue(of({ base64: 'base64-image' } as any));

    component.ngOnInit();

    expect(fileServiceSpy.getParticipantPicture).toHaveBeenCalledWith(component.participant);
    expect(component.participantPicture).toBe('base64-image');
  });

  it('should validate successfully with valid participant', () => {
    const result = (component as any).validate();

    expect(result).toBeTrue();
    expect((component as any).errors.size).toBe(0);
  });

  it('should fail validation when name is empty', () => {
    component.participant.name = '';

    const result = (component as any).validate();

    expect(result).toBeFalse();
    expect((component as any).errors.has(ParticipantFormValidationFields.NAME_ERRORS)).toBeTrue();
  });

  it('should fail validation when lastname is empty', () => {
    component.participant.lastname = '';

    const result = (component as any).validate();

    expect(result).toBeFalse();
    expect((component as any).errors.has(ParticipantFormValidationFields.LASTNAME_ERRORS)).toBeTrue();
  });

  it('should fail validation when idCard exceeds max length', () => {
    component.participant.idCard = 'X'.repeat(100);

    const result = (component as any).validate();

    expect(result).toBeFalse();
    expect((component as any).errors.has(ParticipantFormValidationFields.ID_CARD_ERRORS)).toBeTrue();
  });

  it('should call update when participant has id on save', () => {
    participantServiceSpy.update.and.returnValue(of(component.participant));
    spyOn(component.saved, 'emit');

    component.onSave();

    expect(participantServiceSpy.update).toHaveBeenCalledWith(component.participant);
    expect(component.saved.emit).toHaveBeenCalled();
  });

  it('should call add when participant has no id on save', () => {
    component.participant = buildParticipant(undefined);
    participantServiceSpy.add.and.returnValue(of(component.participant));
    spyOn(component.saved, 'emit');

    component.onSave();

    expect(participantServiceSpy.add).toHaveBeenCalledWith(component.participant);
    expect(component.saved.emit).toHaveBeenCalled();
  });

  it('should show warning and stop save when validation fails', () => {
    component.participant.name = '';

    component.onSave();

    expect(biitSnackbarServiceSpy.showNotification).toHaveBeenCalledWith('validation error', NotificationType.WARNING);
    expect(participantServiceSpy.update).not.toHaveBeenCalled();
    expect(participantServiceSpy.add).not.toHaveBeenCalled();
  });

  it('should set club by id in setClub', () => {
    component.participant.club = undefined as any;

    component.setClub('2');

    expect(component.participant.club!.id).toBe(2);
  });

  it('should delete picture and clear participantPicture', () => {
    component.participantPicture = 'some-image';

    component.deletePicture();

    expect(fileServiceSpy.deleteParticipantPicture).toHaveBeenCalledWith(component.participant);
    expect(messageServiceSpy.infoMessage).toHaveBeenCalledWith('pictureDeleted');
    expect(component.participantPicture).toBeUndefined();
  });
});

