import {Component, Inject, OnInit, Optional} from '@angular/core';
import {Fight} from "../../../models/Fight";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {Action} from "../../../action";

@Component({
  selector: 'app-fight-dialog-box',
  templateUrl: './fight-dialog-box.component.html',
  styleUrls: ['./fight-dialog-box.component.scss']
})
export class FightDialogBoxComponent implements OnInit {

  fight: Fight;
  title: string;
  action: Action;
  actionName: string;

  constructor(
    public dialogRef: MatDialogRef<FightDialogBoxComponent>,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: { title: string, action: Action, entity: Fight }
  ) {
    this.fight = data.entity;
    this.title = data.title;
    this.action = data.action;
    this.actionName = Action[data.action];

  }

  ngOnInit(): void {
  }

}
