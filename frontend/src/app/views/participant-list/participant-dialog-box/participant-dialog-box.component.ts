import {Component, Inject, OnInit, Optional} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {Participant} from "../../../models/participant";
import {Club} from "../../../models/club";
import {UntypedFormControl, UntypedFormGroup, Validators} from "@angular/forms";
import {Observable, startWith} from "rxjs";
import {map} from "rxjs/operators";
import {Action} from "../../../action";
import {RbacBasedComponent} from "../../../components/RbacBasedComponent";
import {RbacService} from "../../../services/rbac/rbac.service";
import {ParticipantPictureDialogBoxComponent} from "./participant-picture/participant-picture-dialog-box.component";
import {PictureUpdatedService} from "../../../services/notifications/picture-updated.service";
import {FileService} from "../../../services/file.service";
import {MessageService} from "../../../services/message.service";

@Component({
  selector: 'app-participant-dialog-box',
  templateUrl: './participant-dialog-box.component.html',
  styleUrls: ['./participant-dialog-box.component.scss']
})
export class ParticipantDialogBoxComponent extends RbacBasedComponent implements OnInit {

  formControl = new UntypedFormControl();
  filteredOptions: Observable<Club[]>;


  participant: Participant;
  title: string;
  action: Action;
  actionName: string;
  clubs: Club[];

  registerForm: UntypedFormGroup;

  participantPicture: string | undefined;

  constructor(
    public dialogRef: MatDialogRef<ParticipantDialogBoxComponent>, rbacService: RbacService,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: { title: string, action: Action, entity: Participant, clubs: Club[] }, public dialog: MatDialog,
    private pictureUpdatedService: PictureUpdatedService, private fileService: FileService, private messageService: MessageService) {
    super(rbacService);
    this.participant = data.entity;
    this.title = data.title;
    this.action = data.action;
    this.actionName = Action[data.action];
    this.clubs = data.clubs;
    this.participantPicture = undefined;

    this.registerForm = new UntypedFormGroup({
      name: new UntypedFormControl(this.participant.name, [Validators.required, Validators.minLength(2), Validators.maxLength(20)]),
      lastname: new UntypedFormControl(this.participant.lastname, [Validators.required, Validators.minLength(2), Validators.maxLength(40)]),
      idCard: new UntypedFormControl(this.participant.idCard, [Validators.required, Validators.maxLength(20)]),
      club: new UntypedFormControl(this.participant.club, [Validators.required])
    },);
  }

  ngOnInit() {
    this.participantPicture = undefined;
    this.filteredOptions = this.formControl.valueChanges.pipe(
      startWith(''),
      map(value => (typeof value === 'string' ? value : value.name)),
      map(name => (name ? this._filter(name) : this.clubs.slice())),
    );
    this.pictureUpdatedService.isPictureUpdated.subscribe(_picture => {
      this.participantPicture = _picture;
    });
    if (this.participant?.id) {
      this.fileService.getParticipantPicture(this.participant).subscribe(_picture => {
        if (_picture) {
          this.participantPicture = _picture.base64;
        } else {
          this.participantPicture = undefined;
        }
      });
    }
  }

  displayClub(club: Club): string {
    return club?.name ? club.name : '';
  }

  private _filter(name: string): Club[] {
    const filterValue = name.toLowerCase();
    return this.clubs.filter(club => club.name.toLowerCase().includes(filterValue));
  }

  doAction() {
    this.participant.name = this.registerForm.get('name')!.value;
    this.participant.lastname = this.registerForm.get('lastname')!.value;
    this.participant.idCard = this.registerForm.get('idCard')!.value;
    this.participant.club = this.registerForm.get('club')!.value;
    this.dialogRef.close({data: this.participant, action: this.action});
  }

  closeDialog() {
    this.dialogRef.close({action: Action.Cancel});
  }

  addPicture() {
    this.openDialog("", Action.Add, this.participant);
  }

  openDialog(title: string, action: Action, participant: Participant) {
    const dialogRef = this.dialog.open(ParticipantPictureDialogBoxComponent, {
      width: '700px',
      data: {
        title: title, action: action, participant: participant
      }
    });
  }

  deletePicture() {
    this.fileService.deleteParticipantPicture(this.participant).subscribe(() => {
      this.messageService.infoMessage("pictureDeleted");
      this.participantPicture = undefined;
    });
  }
}
