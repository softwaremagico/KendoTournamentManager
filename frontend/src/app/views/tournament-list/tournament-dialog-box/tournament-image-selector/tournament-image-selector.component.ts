import {Component, Inject, OnInit, Optional} from '@angular/core';
import {RbacBasedComponent} from "../../../../components/RbacBasedComponent";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {RbacService} from "../../../../services/rbac/rbac.service";
import {Tournament} from "../../../../models/tournament";
import {TranslateService} from "@ngx-translate/core";
import {MessageService} from "../../../../services/message.service";
import {FileService} from "../../../../services/file.service";
import {Action} from "../../../../action";
import {TournamentImageType} from "../../../../models/tournament-image-type";
import {ImageCompression} from "../../../../models/image-compression";

@Component({
  selector: 'app-tournament-image-selector',
  templateUrl: './tournament-image-selector.component.html',
  styleUrls: ['./tournament-image-selector.component.scss']
})
export class TournamentImageSelectorComponent extends RbacBasedComponent implements OnInit {
  tournament: Tournament;
  bannerType: TournamentImageType = TournamentImageType.BANNER;
  accreditationType: TournamentImageType = TournamentImageType.ACCREDITATION;
  diplomaType: TournamentImageType = TournamentImageType.DIPLOMA;
  image: string | null;
  insertedTournamentImageType: TournamentImageType;
  sliderValue: number = 50;

  constructor(@Optional() @Inject(MAT_DIALOG_DATA) public data: { tournament: Tournament },
              public dialogRef: MatDialogRef<TournamentImageSelectorComponent>, rbacService: RbacService, public translateService: TranslateService,
              public messageService: MessageService, public fileService: FileService) {
    super(rbacService);
    this.tournament = data.tournament;
    this.insertedTournamentImageType = this.accreditationType;
    this.refreshImage();
  }

  ngOnInit(): void {

  }

  handleFileInput(event: Event) {
    const element = event.currentTarget as HTMLInputElement;
    let fileList: FileList | null = element.files;
    if (fileList) {
      const file: File | null = fileList.item(0);
      if (!file || file.size < 4096 || file.size > 2097152) {
        const parameters: object = {minSize: '4096', maxSize: '' + (2097152 / (1024 * 1024))};
        this.translateService.get('invalidFileSize', parameters).subscribe((res: string) => {
          this.messageService.errorMessage(res);
        });
      } else {
        const imageCompression: ImageCompression | undefined = ImageCompression.getByType(file!.type);
        if (imageCompression) {
          this.fileService.setTournamentFilePicture(file, this.tournament, this.insertedTournamentImageType, imageCompression).subscribe(_picture => {
            this.messageService.infoMessage('infoPictureStored');
            this.image = _picture.base64;
            this.insertedTournamentImageType = _picture.imageType;
          });
        } else {
          this.messageService.errorMessage('invalidFileSize');

        }
      }
    }
  }

  private refreshImage() {
    this.fileService.getTournamentPicture(this.tournament, this.insertedTournamentImageType).subscribe(_picture => {
      if (_picture) {
        this.image = _picture.base64;
      } else {
        this.image = null;
      }
    });
  }

  closeDialog() {
    this.dialogRef.close({action: Action.Cancel});
  }

  deletePicture(imageType: TournamentImageType) {
    this.fileService.deleteTournamentPicture(this.tournament, imageType).subscribe(_picture => {
      this.messageService.infoMessage('pictureDeleted');
      this.image = null;
    });
  }

  selectType(selectedType: TournamentImageType) {
    this.image = null;
    this.insertedTournamentImageType = selectedType;
    this.refreshImage();
  }

  sliderOnChange(value: number) {
    this.sliderValue = value;
  }

  getHeight(): number {
    if (this.insertedTournamentImageType === TournamentImageType.ACCREDITATION) {
      return 535;
    }
    if (this.insertedTournamentImageType === TournamentImageType.DIPLOMA) {
      return 372;
    }
    if (this.insertedTournamentImageType === TournamentImageType.BANNER) {
      return 135;
    }
    return 0;
  }

  getLinePosition(): number {
    return Math.ceil(this.getHeight() * 0.99 - (this.getHeight() * 0.99 * (this.sliderValue / 100)));
  }
}
