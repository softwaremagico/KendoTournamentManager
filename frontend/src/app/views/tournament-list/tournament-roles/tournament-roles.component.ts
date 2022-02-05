import {Component, Inject, OnInit, Optional} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {ParticipantService} from "../../../services/participant.service";
import {Tournament} from "../../../models/tournament";
import {UserListData} from "../../../components/basic/user-list/user-list-data";
import {CdkDragDrop, moveItemInArray, transferArrayItem} from "@angular/cdk/drag-drop";
import {Participant} from "../../../models/participant";

@Component({
  selector: 'app-tournament-roles',
  templateUrl: './tournament-roles.component.html',
  styleUrls: ['./tournament-roles.component.scss']
})
export class TournamentRolesComponent implements OnInit {

  userListData: UserListData = new UserListData();
  tournament: Tournament;
  competitors: Participant[] = [];
  referee: Participant[] = [];

  constructor(public dialogRef: MatDialogRef<TournamentRolesComponent>,
              public participantService: ParticipantService,
              @Optional() @Inject(MAT_DIALOG_DATA) public data: { tournament: Tournament }) {
    this.tournament = data.tournament;
  }

  ngOnInit(): void {
    this.participantService.getAll().subscribe(participants => {
      this.userListData.participants = participants;
    });
  }

  closeDialog() {
    this.dialogRef.close();
  }

  removeRole(event: CdkDragDrop<string[]>) {
    console.log(event.previousContainer );
    if (event.previousContainer !== event.container) {
      transferArrayItem(this.competitors,
        this.userListData.participants,
        event.previousIndex, event.currentIndex);
    }
  }

  dropCompetitor(event: CdkDragDrop<string[]>) {
    if (event.previousContainer !== event.container) {
      transferArrayItem(this.userListData.participants,
        this.competitors,
        event.previousIndex, event.currentIndex);
    }
  }

  dropReferee(event: CdkDragDrop<string[]>) {
    if (event.previousContainer !== event.container) {
      transferArrayItem(this.userListData.participants,
        this.referee,
        event.previousIndex, event.currentIndex);
    }
  }
}
