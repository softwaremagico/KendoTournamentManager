import {Component, Inject, OnInit, Optional} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {Participant} from "../../../models/participant";
import {Club} from "../../../models/club";
import {FormControl} from "@angular/forms";
import {Observable, startWith} from "rxjs";
import {map} from "rxjs/operators";

export enum Action {
  Add,
  Update,
  Delete,
  Cancel
}

@Component({
  selector: 'app-participant-dialog-box',
  templateUrl: './participant-dialog-box.component.html',
  styleUrls: ['./participant-dialog-box.component.scss']
})
export class ParticipantDialogBoxComponent implements OnInit {

  formControl = new FormControl();
  filteredOptions: Observable<Club[]>;


  participant: Participant;
  title: string;
  action: Action;
  actionName: string;
  clubs: Club[];

  constructor(
    public dialogRef: MatDialogRef<ParticipantDialogBoxComponent>,
    //@Optional() is used to prevent error if no data is passed
    @Optional() @Inject(MAT_DIALOG_DATA) public data: { title: string, action: Action, entity: Participant, clubs: Club[] }) {
    this.participant = data.entity;
    this.title = data.title;
    this.action = data.action;
    this.actionName = Action[data.action];
    this.clubs = data.clubs;
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
    this.dialogRef.close({data: this.participant, action: this.action});
  }

  closeDialog() {
    this.dialogRef.close({action: Action.Cancel});
  }

}
