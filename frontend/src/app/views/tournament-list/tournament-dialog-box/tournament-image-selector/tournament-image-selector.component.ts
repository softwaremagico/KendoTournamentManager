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

  constructor(@Optional() @Inject(MAT_DIALOG_DATA) public data: { tournament: Tournament },
              public dialogRef: MatDialogRef<TournamentImageSelectorComponent>, rbacService: RbacService, public translateService: TranslateService,
              public messageService: MessageService, public fileService: FileService) {
    super(rbacService);
    this.tournament = data.tournament;
  }

  ngOnInit(): void {
  }

  handleFileInput(event: Event, imageType: TournamentImageType) {
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
        this.fileService.setTournamentFilePicture(file, this.tournament, imageType).subscribe(_picture => {
          this.messageService.infoMessage('infoPictureStored');
        });
      }
    }
  }

  closeDialog() {
    this.dialogRef.close({action: Action.Cancel});
  }
}
