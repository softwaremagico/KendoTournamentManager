import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ParticipantPictureComponent} from './participant-picture.component';
import {FileService} from '../../services/file.service';
import {NameUtilsService} from '../../services/name-utils.service';
import {Participant} from '../../models/participant';
import {ParticipantImage} from '../../models/participant-image.model';
import {of} from 'rxjs';

describe('ParticipantPictureComponent', () => {
  let component: ParticipantPictureComponent;
  let fixture: ComponentFixture<ParticipantPictureComponent>;
  let fileServiceSpy: jasmine.SpyObj<FileService>;
  let nameUtilsServiceSpy: jasmine.SpyObj<NameUtilsService>;

  const createParticipant = (id: number, name: string, lastname: string, hasAvatar: boolean = false): Participant => ({
    id,
    name,
    lastname,
    idCard: `ID${id}`,
    hasAvatar,
    locked: false
  } as unknown as Participant);

  beforeEach(async () => {
    fileServiceSpy = jasmine.createSpyObj('FileService', ['getParticipantPicture']);
    nameUtilsServiceSpy = jasmine.createSpyObj('NameUtilsService', ['getInitials']);

    await TestBed.configureTestingModule({
      declarations: [ ParticipantPictureComponent ],
      providers: [
        { provide: FileService, useValue: fileServiceSpy },
        { provide: NameUtilsService, useValue: nameUtilsServiceSpy }
      ]
    })
    .compileComponents();

    fixture = TestBed.createComponent(ParticipantPictureComponent);
    component = fixture.componentInstance;
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize participantWindowOpened as false', () => {
    expect(component.participantWindowOpened).toBeFalse();
  });

  it('should not load picture when participant has no avatar', () => {
    const participant = createParticipant(1, 'John', 'Doe', false);
    component.participant = participant;

    component.ngOnInit();

    expect(fileServiceSpy.getParticipantPicture).not.toHaveBeenCalled();
    expect(component.participantPicture).toBeUndefined();
  });

  it('should load picture when participant has avatar', () => {
    const participant = createParticipant(1, 'John', 'Doe', true);
    const mockImage: ParticipantImage = { base64: 'data:image/png;base64,abc123' } as unknown as ParticipantImage;
    fileServiceSpy.getParticipantPicture.and.returnValue(of(mockImage));
    component.participant = participant;

    component.ngOnInit();

    expect(fileServiceSpy.getParticipantPicture).toHaveBeenCalledOnceWith(participant);
    expect(component.participantPicture).toBe('data:image/png;base64,abc123');
  });

  it('should set participantPicture to undefined when service returns null', () => {
    const participant = createParticipant(1, 'John', 'Doe', true);
    fileServiceSpy.getParticipantPicture.and.returnValue(of(null as unknown as ParticipantImage));
    component.participant = participant;

    component.ngOnInit();

    expect(component.participantPicture).toBeUndefined();
  });

  it('should get initials from nameUtils service', () => {
    const participant = createParticipant(1, 'John', 'Doe');
    nameUtilsServiceSpy.getInitials.and.returnValue('JD');
    component.participant = participant;

    component.ngOnInit();

    expect(nameUtilsServiceSpy.getInitials).toHaveBeenCalledOnceWith(participant);
    expect(component.participantInitials).toBe('JD');
  });

  it('should not call nameUtils when participant is undefined', () => {
    component.participant = undefined;

    component.ngOnInit();

    expect(nameUtilsServiceSpy.getInitials).not.toHaveBeenCalled();
  });

  it('should generate circleStyle for valid participant', () => {
    const participant = createParticipant(1, 'John', 'Doe');
    component.participant = participant;

    const style = component.circleStyle;

    expect(style).toContain('background-color: rgb(');
    expect(style).toContain(' color:rgb(');
  });

  it('should return empty string for circleStyle when participant is undefined', () => {
    component.participant = undefined;

    expect(component.circleStyle).toBe('');
  });

  it('should emit onWindowOpened when openImage is called with true', () => {
    spyOn(component.onWindowOpened, 'emit');

    component.openImage(true);

    expect(component.participantWindowOpened).toBeTrue();
    expect(component.onWindowOpened.emit).toHaveBeenCalledOnceWith(true);
  });

  it('should emit onWindowOpened when openImage is called with false', () => {
    spyOn(component.onWindowOpened, 'emit');

    component.openImage(false);

    expect(component.participantWindowOpened).toBeFalse();
    expect(component.onWindowOpened.emit).toHaveBeenCalledOnceWith(false);
  });

  it('should generate different colors for different participant IDs', () => {
    const participant1 = createParticipant(1, 'John', 'Doe');
    const participant2 = createParticipant(2, 'Jane', 'Smith');

    component.participant = participant1;
    const style1 = component.circleStyle;

    component.participant = participant2;
    const style2 = component.circleStyle;

    expect(style1).not.toBe(style2);
  });
});

