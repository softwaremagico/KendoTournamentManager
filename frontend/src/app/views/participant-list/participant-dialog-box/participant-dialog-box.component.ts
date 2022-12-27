import {Component, Inject, OnInit, Optional} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from '@angular/material/dialog';
import {Participant} from "../../../models/participant";
import {Club} from "../../../models/club";
import {FormControl, FormGroup, Validators} from "@angular/forms";
import {Observable, startWith} from "rxjs";
import {map} from "rxjs/operators";
import {Action} from "../../../action";
import {RbacBasedComponent} from "../../../components/RbacBasedComponent";
import {RbacService} from "../../../services/rbac/rbac.service";
import {ParticipantPictureComponent} from "./participant-picture/participant-picture.component";

@Component({
  selector: 'app-participant-dialog-box',
  templateUrl: './participant-dialog-box.component.html',
  styleUrls: ['./participant-dialog-box.component.scss']
})
export class ParticipantDialogBoxComponent extends RbacBasedComponent implements OnInit {

  formControl = new FormControl();
  filteredOptions: Observable<Club[]>;


  participant: Participant;
  title: string;
  action: Action;
  actionName: string;
  clubs: Club[];

  registerForm: FormGroup;

  constructor(
    public dialogRef: MatDialogRef<ParticipantDialogBoxComponent>, rbacService: RbacService,
    //@Optional() is used to prevent error if no data is passed
    @Optional() @Inject(MAT_DIALOG_DATA) public data: { title: string, action: Action, entity: Participant, clubs: Club[] }, public dialog: MatDialog,) {
    super(rbacService);
    this.participant = data.entity;
    this.title = data.title;
    this.action = data.action;
    this.actionName = Action[data.action];
    this.clubs = data.clubs;

    this.registerForm = new FormGroup({
      name: new FormControl(this.participant.name, [Validators.required, Validators.minLength(2), Validators.maxLength(20)]),
      lastname: new FormControl(this.participant.lastname, [Validators.required, Validators.minLength(2), Validators.maxLength(40)]),
      idCard: new FormControl(this.participant.idCard, [Validators.required, Validators.maxLength(20)]),
      club: new FormControl(this.participant.club?.name, [Validators.required])
    },);
  }

  ngOnInit() {
    this.filteredOptions = this.formControl.valueChanges.pipe(
      startWith(''),
      map(value => (typeof value === 'string' ? value : value.name)),
      map(name => (name ? this._filter(name) : this.clubs.slice())),
    );
  }

  displayClub(club: Club): string {
    return club && club.name ? club.name : '';
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
    const dialogRef = this.dialog.open(ParticipantPictureComponent, {
      width: '700px',
      data: {
        title: title, action: action, entity: participant,
        clubs: this.clubs
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result == undefined) {
        //Do nothing
      } else if (result.action == Action.Add) {
        // this.addRowData(result.data);
      } else if (result.action == Action.Update) {
        // this.updateRowData(result.data);
      } else if (result.action == Action.Delete) {
        // this.deleteRowData(result.data);
      }
    });
  }
}
