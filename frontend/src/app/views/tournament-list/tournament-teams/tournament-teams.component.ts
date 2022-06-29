import {Component, HostListener, Inject, OnInit, Optional,} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {MessageService} from "../../../services/message.service";
import {Tournament} from "../../../models/tournament";
import {RoleType} from "../../../models/role-type";
import {RoleService} from "../../../services/role.service";
import {forkJoin} from "rxjs";
import {Participant} from "../../../models/participant";
import {UserListData} from "../../../components/basic/user-list/user-list-data";
import {CdkDrag, CdkDragDrop, CdkDropList, moveItemInArray, transferArrayItem} from "@angular/cdk/drag-drop";
import {Team} from "../../../models/team";
import {TeamService} from "../../../services/team.service";
import {catchError, tap} from "rxjs/operators";
import {LoggerService} from "../../../services/logger.service";

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
              private loggerService: LoggerService, public teamService: TeamService, public roleService: RoleService,
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
            this.userListData.participants.splice(this.userListData.participants.map(function (p: Participant) {
              return p.id;
            }).indexOf(member.id), 1)
          }
          this.members.set(team, team.members);
        }
        this.userListData.filteredParticipants = this.userListData.participants;
        this.teams = teams;
      }
    });
  }

  @HostListener('document:click', ['$event.target'])
  onClick(element: HTMLElement) {
    if (!element.classList.contains('team-title-editable') && !element.classList.contains('team-header')) {
      if (this.teams) {
        for (let team of this.teams) {
          if (team.editing) {
            team.editing = false;
            this.updateTeamName(team);
          }
        }
      }
    }
  }

  closeDialog() {
    this.dialogRef.close();
  }

  private transferCard(event: CdkDragDrop<Participant[], any>): Participant {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
    } else {
      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex,
      );
    }
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
    this.deleteMemberFromTeam(participant);
    this.userListData.filteredParticipants.sort((a, b) => a.lastname.localeCompare(b.lastname));
    this.userListData.participants.sort((a, b) => a.lastname.localeCompare(b.lastname));
  }

  deleteMemberFromTeam(participant: Participant) {
    this.teamService.deleteByMemberAndTournament(participant, this.tournament).pipe(
      tap(() => {
        this.loggerService.info("Member '" + participant.name + " " + participant.lastname + "' removed.");
      }),
      catchError(this.messageService.handleError<Team>("removing '" + participant.name + " " + participant.lastname + "'"))
    ).subscribe(() => {
      this.messageService.infoMessage("Member '" + participant.name + " " + participant.lastname + "' removed.");
    });
  }

  dropMember(event: CdkDragDrop<Participant[], any>, team: Team) {
    const sourceTeam: Team | undefined = this.searchTeam(event);
    const participant: Participant = this.transferCard(event);
    team.members = this.getMembersContainer(team);
    // Update origin team.
    if (sourceTeam) {
      this.updateTeam(sourceTeam, undefined);
    }
    //Updated destination team.
    this.updateTeam(team, participant);
    //Set default name as the member.
    if (this.tournament.teamSize === 1) {
      team.name = participant.lastname + ", " + participant.name
    }
    if (this.userListData.filteredParticipants.includes(participant)) {
      this.userListData.filteredParticipants.splice(this.userListData.filteredParticipants.indexOf(participant), 1);
    }
    if (this.userListData.participants.includes(participant)) {
      this.userListData.participants.splice(this.userListData.participants.indexOf(participant), 1);
    }
  }

  updateTeam(team: Team, member: Participant | undefined) {
    console.log("team id ", team.id);
    this.teamService.update(team).pipe(
      tap((newTeam: Team) => {
        member ? this.loggerService.info("Team '" + newTeam.name + "' member '" + member.name + " " + member.lastname + "' updated.") :
          this.loggerService.info("Team '" + newTeam.name + "' updated.");
      }),
      catchError(member ? this.messageService.handleError<Team>("Updating '" + member.name + " " + member.lastname + "'") :
        this.messageService.handleError<Team>("Updating '" + team.name + "'"))
    ).subscribe(() => member ? this.messageService.infoMessage("Team '" + Team.name + "' member '" + member.name + " " + member.lastname + "' updated.") : "");
  }

  searchTeam(event: CdkDragDrop<Participant[], any>) {
    const participant: Participant = event.previousContainer.data[event.previousIndex];
    for (let team of [...this.members.keys()]) {
      if (this.getMembersContainer(team).indexOf(participant) !== -1) {
        return team;
      }
    }
    return undefined;
  }

  checkTeamSize(item: CdkDrag, dropList: CdkDropList): boolean {
    const size = dropList.element.nativeElement.getAttribute('data-tournament-size');
    if (!!size) {
      return (dropList.data.length < +size);
    }
    return true;
  }

  setEditable(team: Team, editable: boolean) {
    team.editing = editable;
  }

  updateTeamName(team: Team) {
    this.teamService.update(team).pipe(
      tap((newTeam: Team) => {
        this.loggerService.info("Team name updated to '" + newTeam.name + "'.")
      }),
      catchError(this.messageService.handleError<Team>("Updating team name to '" + team.name + "'."))
    ).subscribe(() => {
      this.messageService.infoMessage("Team name updated to '" + team.name + "'.");
    });
  }

  addTeam(): void {
    const team: Team = new Team();
    team.tournament = this.tournament;

    this.teamService.add(team).pipe(
      tap(() => {
        this.loggerService.info("Adding new team.");
      }),
      catchError(this.messageService.handleError<Team>("Adding new team."))
    ).subscribe(_team => {
      this.messageService.infoMessage("New team '" + _team.name + "' added.");
      this.teams.push(_team);
    });
  }

  deleteTeam(team: Team): void {
    for (let participant of team.members) {
      if (this.userListData.participants.indexOf(participant) < 0) {
        this.userListData.participants.push(participant);
      }
      if (this.userListData.filteredParticipants.indexOf(participant) < 0) {
        this.userListData.filteredParticipants.push(participant);
      }
    }
    this.teamService.delete(team).pipe(
      tap(() => {
        this.loggerService.info("Team '" + team.name + "' removed.");
      }),
      catchError(this.messageService.handleError<Team>("removing team '" + team.name + "'."))
    ).subscribe(() => {
      this.messageService.infoMessage("Team '" + team.name + "' removed.");
      this.teams.splice(this.teams.indexOf(team), 1);
    });
  }
}
