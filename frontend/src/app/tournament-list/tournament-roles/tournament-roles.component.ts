import {Component, Inject, OnInit, Optional} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialog, MatDialogRef} from "@angular/material/dialog";
import {ParticipantService} from "../../services/participant.service";
import {Tournament} from "../../models/tournament";
import {UserListData} from "../../user-list/user-list-data";

@Component({
  selector: 'app-tournament-roles',
  templateUrl: './tournament-roles.component.html',
  styleUrls: ['./tournament-roles.component.scss']
})
export class TournamentRolesComponent implements OnInit {

  userListData: UserListData = new UserListData();
  tournament: Tournament;

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

}
