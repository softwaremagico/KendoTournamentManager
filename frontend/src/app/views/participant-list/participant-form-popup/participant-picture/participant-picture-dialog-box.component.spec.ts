import {of, Subject} from 'rxjs';
import {WebcamImage, WebcamInitError} from 'ngx-webcam';
import {ParticipantPictureDialogBoxComponent} from './participant-picture-dialog-box.component';
import {RbacService} from '../../../../services/rbac/rbac.service';
import {MessageService} from '../../../../services/message.service';
import {FileService} from '../../../../services/file.service';
import {TranslocoService} from '@ngneat/transloco';
import {PictureUpdatedService} from '../../../../services/notifications/picture-updated.service';
import {Participant} from '../../../../models/participant';
import {ParticipantImage} from '../../../../models/participant-image.model';

describe('ParticipantPictureDialogBoxComponent', () => {
  let component: ParticipantPictureDialogBoxComponent;
  let rbacServiceSpy: jasmine.SpyObj<RbacService>;
  let messageServiceSpy: jasmine.SpyObj<MessageService>;
  let fileServiceSpy: jasmine.SpyObj<FileService>;
  let translocoServiceSpy: jasmine.SpyObj<TranslocoService>;
  let pictureUpdatedServiceMock: PictureUpdatedService;

  const createParticipant = (): Participant => ({
    id: 1,
    name: 'John',
    lastname: 'Doe',
    idCard: 'ID-1',
    hasAvatar: false,
    locked: false
  } as unknown as Participant);

  beforeEach(() => {
    rbacServiceSpy = jasmine.createSpyObj('RbacService', ['isAllowed']);
    messageServiceSpy = jasmine.createSpyObj('MessageService', ['infoMessage', 'errorMessage']);
    fileServiceSpy = jasmine.createSpyObj('FileService', [
      'setBase64Picture',
      'setParticipantFilePicture',
      'deleteParticipantPicture'
    ]);
    translocoServiceSpy = jasmine.createSpyObj('TranslocoService', ['translate']);
    pictureUpdatedServiceMock = {
      isPictureUpdated: new Subject<string>()
    } as PictureUpdatedService;

    translocoServiceSpy.translate.and.returnValue('invalid-size');

    component = new ParticipantPictureDialogBoxComponent(
      rbacServiceSpy,
      messageServiceSpy,
      fileServiceSpy,
      translocoServiceSpy,
      pictureUpdatedServiceMock
    );
    component.participant = createParticipant();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should emit onClosed when closeDialog is called', () => {
    spyOn(component.onClosed, 'emit');

    component.closeDialog();

    expect(component.onClosed.emit).toHaveBeenCalled();
  });

  it('should update facing mode and video options when switchCamera is called', () => {
    component.facingMode = 0;

    component.switchCamera();

    expect(component.facingMode).toBe(1);
    expect(component.videoOptions.facingMode).toEqual({ ideal: 'user' } as any);
  });

  it('should emit error message when errorHandler is called', () => {
    const error = { message: 'camera error' } as WebcamInitError;

    component.errorHandler(error);

    expect(messageServiceSpy.errorMessage).toHaveBeenCalledOnceWith('camera error');
  });

  it('should add picture, emit imageClicked and keep only last 5 pictures', () => {
    spyOn(component.imageClicked, 'emit');

    for (let i = 0; i < 6; i++) {
      component.pictureHandler({ imageAsDataUrl: `img-${i}` } as WebcamImage);
    }

    expect(component.pictures.length).toBe(5);
    expect(component.pictures[0]).toBe('img-1');
    expect(component.pictures[4]).toBe('img-5');
    expect(component.imageClicked.emit).toHaveBeenCalledTimes(6);
  });

  it('should trigger clickOnCamera stream and set selected picture on takePicture', () => {
    component.pictures = ['a', 'b', 'c'];
    let emitted = false;
    component.clickOnCamera.subscribe(() => {
      emitted = true;
    });

    component.takePicture();

    expect(emitted).toBeTrue();
    expect(component.selectedPicture).toBe(2);
  });

  it('should save selected base64 image and notify update', () => {
    component.pictures = ['data:image/png;base64,abc'];
    component.selectedPicture = 0;
    const updated = { base64: 'data:image/png;base64,abc' } as ParticipantImage;
    fileServiceSpy.setBase64Picture.and.returnValue(of(updated));
    spyOn(component.onClosed, 'emit');
    const updateSpy = spyOn(pictureUpdatedServiceMock.isPictureUpdated, 'next');

    component.saveImage();

    expect(fileServiceSpy.setBase64Picture).toHaveBeenCalled();
    expect(messageServiceSpy.infoMessage).toHaveBeenCalledWith('infoPictureStored');
    expect(updateSpy).toHaveBeenCalledWith('data:image/png;base64,abc');
    expect(component.participant.hasAvatar).toBeTrue();
    expect(component.onClosed.emit).toHaveBeenCalled();
  });

  it('should toggle selected image when same image is clicked twice', () => {
    component.selectImage(2);
    expect(component.selectedPicture).toBe(2);

    component.selectImage(2);
    expect(component.selectedPicture).toBeUndefined();
  });

  it('should show invalid size error when selected file size is out of range', () => {
    const tiny = new File(['x'], 'tiny.png', { type: 'image/png' });
    const input = document.createElement('input');
    const fileList = {
      length: 1,
      item: (_index: number) => tiny
    } as unknown as FileList;

    Object.defineProperty(input, 'files', {
      configurable: true,
      get: () => fileList
    });

    component.handleFileInput({ currentTarget: input } as unknown as Event);

    expect(messageServiceSpy.errorMessage).toHaveBeenCalledWith('invalid-size');
    expect(fileServiceSpy.setParticipantFilePicture).not.toHaveBeenCalled();
  });

  it('should upload selected file when file size is valid', () => {
    const validContent = 'x'.repeat(5000);
    const file = new File([validContent], 'valid.png', { type: 'image/png' });
    const input = document.createElement('input');
    const fileList = {
      length: 1,
      item: (_index: number) => file
    } as unknown as FileList;

    Object.defineProperty(input, 'files', {
      configurable: true,
      get: () => fileList
    });

    fileServiceSpy.setParticipantFilePicture.and.returnValue(of({ base64: 'img' } as ParticipantImage));
    spyOn(component.onClosed, 'emit');
    const updateSpy = spyOn(pictureUpdatedServiceMock.isPictureUpdated, 'next');

    component.handleFileInput({ currentTarget: input } as unknown as Event);

    expect(fileServiceSpy.setParticipantFilePicture).toHaveBeenCalledOnceWith(file, component.participant);
    expect(messageServiceSpy.infoMessage).toHaveBeenCalledWith('infoPictureStored');
    expect(updateSpy).toHaveBeenCalledWith('img');
    expect(component.onClosed.emit).toHaveBeenCalled();
  });

  it('should delete picture and show confirmation message', () => {
    fileServiceSpy.deleteParticipantPicture.and.returnValue(of(undefined));

    component.deletePicture();

    expect(fileServiceSpy.deleteParticipantPicture).toHaveBeenCalledOnceWith(component.participant);
    expect(messageServiceSpy.infoMessage).toHaveBeenCalledWith('pictureDeleted');
  });
});

