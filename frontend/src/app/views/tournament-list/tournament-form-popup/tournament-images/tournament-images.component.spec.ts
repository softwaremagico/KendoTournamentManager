import { of } from 'rxjs';
import { TournamentImagesComponent } from './tournament-images.component';
import { RbacService } from '../../../../services/rbac/rbac.service';
import { MessageService } from '../../../../services/message.service';
import { FileService } from '../../../../services/file.service';
import { TranslocoService } from '@ngneat/transloco';
import { TournamentExtendedPropertiesService } from '../../../../services/tournament-extended-properties.service';
import { TournamentService } from '../../../../services/tournament.service';
import { Tournament } from '../../../../models/tournament';
import { TournamentScore } from '../../../../models/tournament-score.model';
import { TournamentImageType } from '../../../../models/tournament-image-type';
import { TournamentExtendedProperty } from '../../../../models/tournament-extended-property.model';

describe('TournamentImagesComponent', () => {
  let component: TournamentImagesComponent;
  let rbacServiceSpy: jasmine.SpyObj<RbacService>;
  let messageServiceSpy: jasmine.SpyObj<MessageService>;
  let fileServiceSpy: jasmine.SpyObj<FileService>;
  let translocoServiceSpy: jasmine.SpyObj<TranslocoService>;
  let tournamentExtendedPropertiesServiceSpy: jasmine.SpyObj<TournamentExtendedPropertiesService>;
  let tournamentServiceSpy: jasmine.SpyObj<TournamentService>;

  const buildTournament = (): Tournament => {
    const t = new Tournament();
    t.id = 1;
    t.name = 'Test Tournament';
    t.tournamentScore = new TournamentScore();
    return t;
  };

  const buildPicture = (base64: string) => ({ base64 });

  beforeEach(() => {
    rbacServiceSpy = jasmine.createSpyObj('RbacService', ['isAllowed']);
    messageServiceSpy = jasmine.createSpyObj('MessageService', ['infoMessage', 'errorMessage', 'handleError']);
    fileServiceSpy = jasmine.createSpyObj('FileService', [
      'getTournamentPicture',
      'setTournamentFilePicture',
      'deleteTournamentPicture'
    ]);
    translocoServiceSpy = jasmine.createSpyObj('TranslocoService', ['translate']);
    tournamentExtendedPropertiesServiceSpy = jasmine.createSpyObj(
      'TournamentExtendedPropertiesService',
      ['getByTournamentAndKey', 'update']
    );
    tournamentServiceSpy = jasmine.createSpyObj('TournamentService', [
      'getParticipantDiploma',
      'getParticipantAccreditation'
    ]);

    rbacServiceSpy.isAllowed.and.returnValue(true);
    fileServiceSpy.getTournamentPicture.and.returnValue(of(null) as any);
    fileServiceSpy.deleteTournamentPicture.and.returnValue(of(null) as any);
    tournamentExtendedPropertiesServiceSpy.getByTournamentAndKey.and.returnValue(of(null) as any);
    tournamentExtendedPropertiesServiceSpy.update.and.returnValue(of({} as TournamentExtendedProperty));
    translocoServiceSpy.translate.and.returnValue('translated text');
    tournamentServiceSpy.getParticipantDiploma.and.returnValue(of(new Blob(['test'])));
    tournamentServiceSpy.getParticipantAccreditation.and.returnValue(of(new Blob(['test'])));

    component = new TournamentImagesComponent(
      rbacServiceSpy,
      messageServiceSpy,
      fileServiceSpy,
      translocoServiceSpy,
      tournamentExtendedPropertiesServiceSpy,
      tournamentServiceSpy
    );
    component.tournament = buildTournament();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should load images on ngOnInit', () => {
    component.ngOnInit();

    expect(fileServiceSpy.getTournamentPicture).toHaveBeenCalledWith(component.tournament, TournamentImageType.DIPLOMA);
    expect(fileServiceSpy.getTournamentPicture).toHaveBeenCalledWith(component.tournament, TournamentImageType.BANNER);
    expect(fileServiceSpy.getTournamentPicture).toHaveBeenCalledWith(component.tournament, TournamentImageType.PHOTO);
    expect(fileServiceSpy.getTournamentPicture).toHaveBeenCalledWith(component.tournament, TournamentImageType.ACCREDITATION);
  });

  it('should set image properties when pictures are loaded', () => {
    fileServiceSpy.getTournamentPicture.and.callFake((_tournament: any, type: any) => {
      return of({ base64: 'base64img_' + type } as any);
    });

    component.ngOnInit();

    expect((component as any).diplomaImage).toBe('base64img_' + TournamentImageType.DIPLOMA);
    expect((component as any).bannerImage).toBe('base64img_' + TournamentImageType.BANNER);
    expect((component as any).accreditationImage).toBe('base64img_' + TournamentImageType.ACCREDITATION);
  });

  it('should set image properties to null when pictures are not found', () => {
    fileServiceSpy.getTournamentPicture.and.returnValue(of(null) as any);

    component.ngOnInit();

    expect((component as any).diplomaImage).toBeNull();
    expect((component as any).bannerImage).toBeNull();
    expect((component as any).accreditationImage).toBeNull();
  });

  it('should set nameLine from extended property in ngOnInit', () => {
    tournamentExtendedPropertiesServiceSpy.getByTournamentAndKey.and.returnValue(
      of({ propertyValue: '0.5' } as TournamentExtendedProperty)
    );

    component.ngOnInit();

    expect((component as any).nameLine).toBe(50);
  });

  it('should set nameLine to componentHeight/2 when property is zero', () => {
    tournamentExtendedPropertiesServiceSpy.getByTournamentAndKey.and.returnValue(
      of({ propertyValue: '0' } as any)
    );

    component.ngOnInit();

    expect((component as any).nameLine).toBe((component as any).componentHeight / 2);
  });

  it('should set nameLine to 50 when property is null', () => {
    tournamentExtendedPropertiesServiceSpy.getByTournamentAndKey.and.returnValue(of(null) as any);

    component.ngOnInit();

    expect((component as any).nameLine).toBe(50);
  });

  it('should increment nameLine by 1 when moveLineUp is called', () => {
    (component as any).nameLine = 40;
    tournamentExtendedPropertiesServiceSpy.update.and.returnValue(of({} as TournamentExtendedProperty));

    component.moveLineUp();

    expect((component as any).nameLine).toBe(41);
  });

  it('should increment nameLine by 5 when moveLineUpFast is called', () => {
    (component as any).nameLine = 40;

    component.moveLineUpFast();

    expect((component as any).nameLine).toBe(45);
  });

  it('should decrement nameLine by 1 when moveLineDown is called', () => {
    (component as any).nameLine = 40;

    component.moveLineDown();

    expect((component as any).nameLine).toBe(39);
  });

  it('should decrement nameLine by 5 when moveLineDownFast is called', () => {
    (component as any).nameLine = 40;

    component.moveLineDownFast();

    expect((component as any).nameLine).toBe(35);
  });

  it('should clamp nameLine to 0 when going below 0', () => {
    (component as any).nameLine = 2;

    component.moveLineDownFast();

    expect((component as any).nameLine).toBe(0);
  });

  it('should clamp nameLine to 100 when exceeding 100', () => {
    (component as any).nameLine = 98;

    component.moveLineUpFast();

    expect((component as any).nameLine).toBe(100);
  });

  it('should calculate line position correctly in getLinePosition', () => {
    const componentHeight = (component as any).componentHeight;
    (component as any).nameLine = 50;

    const position = component.getLinePosition();

    expect(position).toBe(Math.ceil(componentHeight * 0.99 - componentHeight * 0.99 * 0.5));
  });

  it('should delete picture and refresh images when deletePicture is called', () => {
    spyOn(component as any, 'refreshImages');

    component.deletePicture(TournamentImageType.BANNER);

    expect(fileServiceSpy.deleteTournamentPicture).toHaveBeenCalledWith(component.tournament, TournamentImageType.BANNER);
    expect(messageServiceSpy.infoMessage).toHaveBeenCalledWith('pictureDeleted');
    expect((component as any).refreshImages).toHaveBeenCalled();
  });

  it('should download diploma preview and trigger file download', () => {
    spyOn(window.URL, 'createObjectURL').and.returnValue('blob:http://localhost');
    spyOn(document, 'createElement').and.callThrough();

    component.downloadPreview(TournamentImageType.DIPLOMA);

    expect(tournamentServiceSpy.getParticipantDiploma).toHaveBeenCalledWith(1, jasmine.any(Object));
  });

  it('should download accreditation preview for non-diploma types', () => {
    spyOn(window.URL, 'createObjectURL').and.returnValue('blob:http://localhost');

    component.downloadPreview(TournamentImageType.ACCREDITATION);

    expect(tournamentServiceSpy.getParticipantAccreditation).toHaveBeenCalledWith(1, jasmine.any(Object), undefined);
  });

  it('should show error when uploaded file is too small', () => {
    const file = new File(['hi'], 'image.png', { type: 'image/png' });
    const input = document.createElement('input');
    Object.defineProperty(input, 'files', {
      get: () => ({ item: () => file, length: 1 } as any)
    });

    component.handleFileInput({ currentTarget: input } as any, TournamentImageType.DIPLOMA);

    expect(messageServiceSpy.errorMessage).toHaveBeenCalledWith('translated text');
  });
});






