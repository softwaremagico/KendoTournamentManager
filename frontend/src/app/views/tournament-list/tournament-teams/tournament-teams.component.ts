import {Component, Inject, OnInit, Optional} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {MessageService} from "../../../services/message.service";
import {Tournament} from "../../../models/tournament";
import {RoleType} from "../../../models/role-type";
import {RoleService} from "../../../services/role.service";
import {forkJoin} from "rxjs";
import {Participant} from "../../../models/participant";
import {UserListData} from "../../../components/basic/user-list/user-list-data";
import {CdkDragDrop, transferArrayItem} from "@angular/cdk/drag-drop";
import {Team} from "../../../models/team";
import {TeamService} from "../../../services/team.service";

@Component({
  selector: 'app-tournament-teams',
  templateUrl: './tournament-teams.component.html',
  styleUrls: ['./tournament-teams.component.scss']
})
export class TournamentTeamsComponent implements OnInit {

  userListData: UserListData = new UserListData();
  tournament: Tournament;
  teams: Team[];
  members = new Map<Team, Participant[]>();

  constructor(public dialogRef: MatDialogRef<TournamentTeamsComponent>, private messageService: MessageService,
              public teamService: TeamService, public roleService: RoleService,
              @Optional() @Inject(MAT_DIALOG_DATA) public data: { tournament: Tournament }) {
    this.tournament = data.tournament;
  }

  getMembersContainer(team: Team): Participant[] {
    if (this.members.get(team) === undefined) {
      this.members.set(team, []);
    }
    return this.members.get(team)!;
  }

  ngOnInit(): void {
    let teamsRequest = this.teamService.getFromTournament(this.tournament);
    let roleRequests = this.roleService.getFromTournamentAndType(this.tournament.id!, RoleType.COMPETITOR);
    forkJoin([teamsRequest, roleRequests]).subscribe(([teams, roles]) => {
      if (roles === undefined) {
        roles = [];
      }
      this.userListData.participants = roles.map(role => role.participant);
      if (teams !== undefined) {
        for (let team of teams) {
          for (let member of team.members) {
            this.userListData.participants.splice(this.userListData.participants.indexOf(member), 1)
          }
        }
        this.userListData.filteredParticipants = this.userListData.participants;
        this.teams = teams;
      }
    });
  }

  closeDialog() {
    this.dialogRef.close();
  }

  private transferCard(event: CdkDragDrop<Participant[], any>): Participant {
    transferArrayItem(
      event.previousContainer.data,
      event.container.data,
      this.userListData.getRealIndex(event.previousIndex),
      event.currentIndex,
    );
    return event.container.data[event.currentIndex];
  }

  removeFromTeam(event: CdkDragDrop<Participant[], any>) {
    transferArrayItem(
      event.previousContainer.data,
      event.container.data,
      event.previousIndex,
      event.currentIndex,
    );
    const participant: Participant = event.container.data[event.currentIndex];
    this.teamService.deleteByMemberAndTournament(participant, this.tournament).subscribe(team => {
      this.messageService.infoMessage("Member '" + participant + "' removed.");
    });
  }

  dropMember(event: CdkDragDrop<Participant[], any>, team: Team) {
    const participant: Participant = this.transferCard(event);
    team.members = this.getMembersContainer(team);
    this.teamService.update(team).subscribe(team => {
      this.messageService.infoMessage("Team '" + Team + "' member '" + participant + "' updated.");
    });
  }
}
