import {Component, Inject, OnInit, Optional} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {ParticipantService} from "../../../services/participant.service";
import {Tournament} from "../../../models/tournament";
import {UserListData} from "../../../components/basic/user-list/user-list-data";
import {CdkDragDrop, transferArrayItem} from "@angular/cdk/drag-drop";
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
  referees: Participant[] = [];

  constructor(public dialogRef: MatDialogRef<TournamentRolesComponent>,
              public participantService: ParticipantService,
              @Optional() @Inject(MAT_DIALOG_DATA) public data: { tournament: Tournament }) {
    this.tournament = data.tournament;
  }

  ngOnInit(): void {
    this.participantService.getAll().subscribe(participants => {
      this.userListData.participants = participants;
      this.userListData.filteredParticipants = participants;
    });
  }

  closeDialog() {
    this.dialogRef.close();
  }

  transferCard(event: CdkDragDrop<Participant[], any>) {
    transferArrayItem(
      event.previousContainer.data,
      event.container.data,
      event.previousIndex,
      event.currentIndex,
    );
  }

  removeRole(event: CdkDragDrop<Participant[], any>) {
    this.transferCard(event);
  }

  dropCompetitor(event: CdkDragDrop<Participant[], any>) {
    this.transferCard(event);
  }

  dropReferee(event: CdkDragDrop<Participant[], any>) {
    this.transferCard(event);
  }
}
