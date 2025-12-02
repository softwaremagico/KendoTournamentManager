import {Component, Inject, OnInit, Optional} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {Participant} from "../../../models/participant";
import {Club} from "../../../models/club";
import {UntypedFormControl, UntypedFormGroup, Validators} from "@angular/forms";
import {Action} from "../../../action";
import {RbacBasedComponent} from "../../../components/RbacBasedComponent";
import {RbacService} from "../../../services/rbac/rbac.service";
import {ParticipantPictureDialogBoxComponent} from "./participant-picture/participant-picture-dialog-box.component";
import {PictureUpdatedService} from "../../../services/notifications/picture-updated.service";
import {FileService} from "../../../services/file.service";
import {MessageService} from "../../../services/message.service";
import {ParticipantImage} from "../../../models/participant-image.model";
import {RbacActivity} from "../../../services/rbac/rbac.activity";
import {InputLimits} from "../../../utils/input-limits";
import {CsvService} from "../../../services/csv-service";
import {TranslocoService} from "@ngneat/transloco";

@Component({
  selector: 'app-participant-dialog-box',
  templateUrl: './participant-dialog-box.component.html',
  styleUrls: ['./participant-dialog-box.component.scss']
})
export class ParticipantDialogBoxComponent extends RbacBasedComponent implements OnInit {

  protected USER_NAME_MIN_LENGTH: number = InputLimits.MIN_FIELD_LENGTH;
  protected USER_NAME_MAX_LENGTH: number = InputLimits.MAX_NORMAL_FIELD_LENGTH;
  protected USER_LASTNAME_MIN_LENGTH: number = InputLimits.MIN_FIELD_LENGTH;
  protected USER_LASTNAME_MAX_LENGTH: number = InputLimits.MAX_NORMAL_FIELD_LENGTH;
  protected USER_ID_MAX_LENGTH: number = InputLimits.MAX_SMALL_FIELD_LENGTH;


  participant: Participant;
  title: string;
  action: Action;
  actionName: string;
  clubs: Club[];

  registerForm: UntypedFormGroup;

  participantPicture: string | undefined;

  constructor(
    public dialogRef: MatDialogRef<ParticipantDialogBoxComponent>, rbacService: RbacService, public csvService: CsvService,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: {
      title: string,
      action: Action,
      entity: Participant,
      clubs: Club[]
    }, public dialog: MatDialog, private translateService: TranslocoService,
    private pictureUpdatedService: PictureUpdatedService, private fileService: FileService, private messageService: MessageService) {
    super(rbacService);
    this.participant = data.entity;
    this.title = data.title;
    this.action = data.action;
    this.actionName = Action[data.action];
    this.clubs = data.clubs;
    this.participantPicture = undefined;

    this.registerForm = new UntypedFormGroup({
      name: new UntypedFormControl({
        value: this.participant.name,
        disabled: !rbacService.isAllowed(RbacActivity.EDIT_PARTICIPANT)
      }, [Validators.required, Validators.minLength(this.USER_NAME_MIN_LENGTH), Validators.maxLength(this.USER_NAME_MAX_LENGTH)]),
      lastname: new UntypedFormControl({
        value: this.participant.lastname,
        disabled: !rbacService.isAllowed(RbacActivity.EDIT_PARTICIPANT)
      }, [Validators.required, Validators.minLength(this.USER_LASTNAME_MIN_LENGTH), Validators.maxLength(this.USER_LASTNAME_MAX_LENGTH)]),
      idCard: new UntypedFormControl({
        value: this.participant.idCard,
        disabled: !rbacService.isAllowed(RbacActivity.EDIT_PARTICIPANT)
      }, [Validators.maxLength(this.USER_ID_MAX_LENGTH)]),
      club: new UntypedFormControl({
        value: this.participant.club,
        disabled: !rbacService.isAllowed(RbacActivity.EDIT_PARTICIPANT)
      }, [Validators.required])
    },);
  }

  ngOnInit(): void {
    this.participantPicture = undefined;
    this.pictureUpdatedService.isPictureUpdated.subscribe((_picture: string): void => {
      this.participantPicture = _picture;
    });
    if (this.participant?.id) {
      this.fileService.getParticipantPicture(this.participant).subscribe((_picture: ParticipantImage): void => {
        if (_picture) {
          this.participantPicture = _picture.base64;
        } else {
          this.participantPicture = undefined;
        }
      });
    }
  }

  compareClubs(club1: any, club2: any): boolean {
    if (club2 == undefined && club1 == undefined) {
      return true;
    }
    if ((club2 == undefined && club1 != undefined) || (club2 != undefined && club1 == undefined)) {
      return false
    }
    return club1.name === club2.name && club1.id === club2.id;
  }

  displayClub(club: Club): string {
    return club?.name ? club.name : '';
  }

  private _filter(name: string): Club[] {
    const filterValue: string = name.toLowerCase();
    return this.clubs.filter((club: Club) => club.name.toLowerCase().includes(filterValue));
  }

  doAction(): void {
    this.participant.name = this.registerForm.get('name')!.value;
    this.participant.lastname = this.registerForm.get('lastname')!.value;
    this.participant.idCard = this.registerForm.get('idCard')!.value;
    this.participant.club = this.registerForm.get('club')!.value;
    this.dialogRef.close({data: this.participant, action: this.action});
  }

  closeDialog(): void {
    this.dialogRef.close({action: Action.Cancel});
  }

  addPicture(): void {
    this.openDialog("", Action.Add, this.participant);
  }

  openDialog(title: string, action: Action, participant: Participant): void {
    const dialogRef = this.dialog.open(ParticipantPictureDialogBoxComponent, {
      panelClass: 'pop-up-panel',
      width: '700px',
      data: {
        title: title, action: action, participant: participant
      }
    });
  }

  deletePicture(): void {
    this.fileService.deleteParticipantPicture(this.participant).subscribe((): void => {
      this.messageService.infoMessage("pictureDeleted");
      this.participantPicture = undefined;
    });
  }


  handleFileInput(event: Event) {
    const element = event.currentTarget as HTMLInputElement;
    let fileList: FileList | null = element.files;
    if (fileList) {
      const file: File | null = fileList.item(0);
      if (file) {
        this.csvService.addParticipants(file).subscribe(_participants => {
          if (_participants.length == 0) {
            this.messageService.infoMessage('infoParticipantStored');
            //We cancel action or will be saved later again.
            this.dialogRef.close({action: Action.Cancel});
          } else {
            const parameters: object = {element: _participants[0].name};
            this.messageService.errorMessage(this.translateService.translate('failedOnCsvField', parameters));
          }
        });
      }
    }
  }
}
