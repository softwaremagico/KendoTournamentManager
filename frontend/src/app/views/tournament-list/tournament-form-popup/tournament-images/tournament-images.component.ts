import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Tournament} from "../../../../models/tournament";
import {BiitProgressBarType} from "@biit-solutions/wizardry-theme/info";
import {TournamentExtendedProperty} from "../../../../models/tournament-extended-property.model";
import {TournamentExtraPropertyKey} from "../../../../models/tournament-extra-property-key";
import {TournamentExtendedPropertiesService} from "../../../../services/tournament-extended-properties.service";
import {RbacBasedComponent} from "../../../../components/RbacBasedComponent";
import {RbacService} from "../../../../services/rbac/rbac.service";
import {MessageService} from "../../../../services/message.service";
import {FileService} from "../../../../services/file.service";
import {ImageCompression} from "../../../../models/image-compression";
import {TournamentImageType} from "../../../../models/tournament-image-type";
import {TranslocoService} from "@ngneat/transloco";
import {Participant} from "../../../../models/participant";
import {TournamentService} from "../../../../services/tournament.service";

@Component({
  selector: 'tournament-images',
  templateUrl: './tournament-images.component.html',
  styleUrls: ['./tournament-images.component.scss']
})
export class TournamentImagesComponent extends RbacBasedComponent implements OnInit {
  @Input()
  tournament: Tournament;
  @Output() onClosed: EventEmitter<void> = new EventEmitter<void>();
  protected readonly BiitProgressBarType = BiitProgressBarType;
  protected loading: boolean = false;
  protected diplomaImage: string | null;
  protected bannerImage: string | null;
  protected accreditationImage: string | null;
  protected defaultPhotoImage: string | null;
  protected componentHeight: number = 600; //Same as CSS
  protected nameLine: number = 300;

  protected readonly TournamentImageType = TournamentImageType;

  constructor(rbacService: RbacService,
              public messageService: MessageService, public fileService: FileService, public transloco: TranslocoService,
              private tournamentExtendedPropertiesService: TournamentExtendedPropertiesService, private tournamentService: TournamentService) {
    super(rbacService);
  }

  ngOnInit(): void {
    this.refreshNameLine();
    this.refreshImages();
  }

  handleFileInput(event: Event, tournamentImageType: TournamentImageType) {
    const element = event.currentTarget as HTMLInputElement;
    let fileList: FileList | null = element.files;
    if (fileList) {
      const file: File | null = fileList.item(0);
      if (!file || file.size < 4096 || file.size > 2097152) {
        const parameters: object = {minSize: '4096', maxSize: '' + (2097152 / (1024 * 1024))};
        this.messageService.errorMessage(this.transloco.translate('invalidFileSize', parameters));
      } else {
        const imageCompression: ImageCompression | undefined = ImageCompression.getByType(file.type);
        if (imageCompression) {
          this.fileService.setTournamentFilePicture(file, this.tournament, tournamentImageType, imageCompression).subscribe(_picture => {
            this.messageService.infoMessage('infoPictureStored');
            if (tournamentImageType == TournamentImageType.ACCREDITATION) {
              this.accreditationImage = _picture.base64;
            } else if (tournamentImageType == TournamentImageType.BANNER) {
              this.bannerImage = _picture.base64;
            } else if (tournamentImageType == TournamentImageType.DIPLOMA) {
              this.diplomaImage = _picture.base64;
            } else if (tournamentImageType == TournamentImageType.PHOTO) {
              this.defaultPhotoImage = _picture.base64;
            }
          });
        } else {
          this.messageService.errorMessage('invalidFileSize');
        }
      }
    }
  }

  private refreshNameLine() {
    this.tournamentExtendedPropertiesService.getByTournamentAndKey(this.tournament, TournamentExtraPropertyKey.DIPLOMA_NAME_HEIGHT).subscribe(_tournamentProperty => {
      if (_tournamentProperty) {
        this.nameLine = parseFloat(_tournamentProperty.propertyValue) * 100;
        if (this.nameLine == 0) {
          this.nameLine = this.componentHeight / 2;
        }
      } else {
        this.nameLine = 50;
      }
    });
  }

  private refreshImages() {
    this.fileService.getTournamentPicture(this.tournament, TournamentImageType.DIPLOMA).subscribe(_picture => {
      if (_picture) {
        this.diplomaImage = _picture.base64;
      } else {
        this.diplomaImage = null;
      }
    });
    this.fileService.getTournamentPicture(this.tournament, TournamentImageType.BANNER).subscribe(_picture => {
      if (_picture) {
        this.bannerImage = _picture.base64;
      } else {
        this.bannerImage = null;
      }
    });
    this.fileService.getTournamentPicture(this.tournament, TournamentImageType.PHOTO).subscribe(_picture => {
      if (_picture) {
        this.defaultPhotoImage = _picture.base64;
      } else {
        this.defaultPhotoImage = null;
      }
    });
    this.fileService.getTournamentPicture(this.tournament, TournamentImageType.ACCREDITATION).subscribe(_picture => {
      if (_picture) {
        this.accreditationImage = _picture.base64;
      } else {
        this.accreditationImage = null;
      }
    });
  }

  moveLineUp() {
    this.nameLine++;
    this.updateNamePosition();
  }

  moveLineUpFast() {
    this.nameLine += 5;
    this.updateNamePosition();
  }

  moveLineDown() {
    this.nameLine--;
    this.updateNamePosition();
  }

  moveLineDownFast() {
    this.nameLine -= 5;
    this.updateNamePosition();
  }

  getLinePosition(): number {
    return Math.ceil(this.componentHeight * 0.99 - (this.componentHeight * 0.99 * (this.nameLine / 100)));
  }

  updateNamePosition() {
    if (this.nameLine < 0) {
      this.nameLine = 0;
    }
    if (this.nameLine > 100) {
      this.nameLine = 100;
    }
    const tournamentProperty: TournamentExtendedProperty = new TournamentExtendedProperty();
    tournamentProperty.tournament = this.tournament;
    tournamentProperty.propertyValue = (this.nameLine / 100).toString();
    tournamentProperty.propertyKey = TournamentExtraPropertyKey.DIPLOMA_NAME_HEIGHT;
    this.tournamentExtendedPropertiesService.update(tournamentProperty).subscribe();
  }

  deletePicture(imageType: TournamentImageType, image: string | null) {
    this.fileService.deleteTournamentPicture(this.tournament, imageType).subscribe(_picture => {
      this.messageService.infoMessage('pictureDeleted');
      image = null;
      this.refreshImages();
    });
  }

  downloadPreview(insertedTournamentImageType: TournamentImageType) {
    if (this.tournament!.id) {
      const participant: Participant = new Participant();

      const res: string = this.transloco.translate('nameExample');
      const names: string[] = res.split(' ');
      participant.name = names[0];
      participant.lastname = names[1];

      if (insertedTournamentImageType === TournamentImageType.DIPLOMA) {
        this.tournamentService.getParticipantDiploma(this.tournament.id, participant).subscribe((html: Blob) => {
          const blob = new Blob([html], {type: 'application/pdf'});
          const downloadURL = window.URL.createObjectURL(blob);

          const anchor = document.createElement("a");
          anchor.download = insertedTournamentImageType + ".pdf";
          anchor.href = downloadURL;
          anchor.click();
        });
      } else {
        this.tournamentService.getParticipantAccreditation(this.tournament.id, participant, undefined).subscribe((html: Blob) => {
          const blob = new Blob([html], {type: 'application/pdf'});
          const downloadURL = window.URL.createObjectURL(blob);

          const anchor = document.createElement("a");
          anchor.download = insertedTournamentImageType + ".pdf";
          anchor.href = downloadURL;
          anchor.click();
        });
      }
    }
  }
}
