import {Component, HostListener, Inject, OnInit, Optional,} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {MessageService} from "../../../services/message.service";
import {Tournament} from "../../../models/tournament";
import {RoleType} from "../../../models/role-type";
import {RoleService} from "../../../services/role.service";
import {forkJoin} from "rxjs";
import {Participant} from "../../../models/participant";
import {UserListData} from "../../../components/basic/user-list/user-list-data";
import {CdkDrag, CdkDragDrop, CdkDropList, moveItemInArray} from "@angular/cdk/drag-drop";
import {Team} from "../../../models/team";
import {TeamService} from "../../../services/team.service";
import {catchError, tap} from "rxjs/operators";
import {LoggerService} from "../../../services/logger.service";
import {NameUtilsService} from "../../../services/name-utils.service";
import {SystemOverloadService} from "../../../services/system-overload.service";
import {Club} from "../../../models/club";

@Component({
  selector: 'app-tournament-teams',
  templateUrl: './tournament-teams.component.html',
  styleUrls: ['./tournament-teams.component.scss']
})
export class TournamentTeamsComponent implements OnInit {

  userListData: UserListData = new UserListData();
  tournament: Tournament;
  teams: Team[];
  members = new Map<Team, (Participant | undefined)[]>();

  constructor(public dialogRef: MatDialogRef<TournamentTeamsComponent>, private messageService: MessageService,
              private loggerService: LoggerService, public teamService: TeamService, public roleService: RoleService,
              public nameUtilsService: NameUtilsService, private systemOverloadService: SystemOverloadService,
              @Optional() @Inject(MAT_DIALOG_DATA) public data: { tournament: Tournament }) {
    this.tournament = data.tournament;
  }

  getMembersContainer(team: Team): (Participant | undefined)[] {
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
        teams.sort(function (a, b) {
          return a.name.localeCompare(b.name);
        });
        for (let team of teams) {
          for (let member of team.members) {
            if (member) {
              this.userListData.participants.splice(this.userListData.participants.map(function (p: Participant) {
                return p.id;
              }).indexOf(member.id), 1)
            }
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

  getMember(team: Team, index: number): Participant | undefined {
    return this.getMembersContainer(team)[index];
  }

  getCardTitle(team: Team, index: number): string {
    const member: Participant | undefined = this.getMember(team, index);
    if (member) {
      return member.lastname + ", " + member.name;
    }
    return "";
  }

  getCardSubTitle(team: Team, index: number): string {
    const member: Participant | undefined = this.getMember(team, index);
    if (member) {
      const club: Club | undefined = member.club;
      if (club) {
        return club.name;
      }
    }
    return "";
  }

  closeDialog() {
    this.dialogRef.close();
  }

  private transferCard(event: CdkDragDrop<(Participant | undefined)[], any>, memberIndex: number): Participant | undefined {
    if (event.previousContainer === event.container) {
      moveItemInArray(event.container.data, event.previousIndex, memberIndex);
    } else {
      const translatedTeamMember = event.previousContainer.data[event.previousIndex];
      event.previousContainer.data.splice(event.previousIndex, 1);
      event.container.data[memberIndex] = translatedTeamMember;
    }
    return event.container.data[memberIndex];
  }

  removeFromTeam(event: CdkDragDrop<Participant[], any>) {
    // Correct index, as always return the first non-empty.
    const sourceTeam: Team | undefined = this.searchTeam(event as CdkDragDrop<(Participant | undefined)[], any>);
    const movedParticipant: Participant = event.item.data;

    //Remove from source data.
    if (sourceTeam && this.members) {
      const sourceIndex: number | undefined = this.members.get(sourceTeam)?.indexOf(movedParticipant);
      if (sourceIndex || sourceIndex === 0) {
        this.members.get(sourceTeam)?.splice(sourceIndex, 1);
        this.deleteMemberFromTeam(movedParticipant);
        //Add to user list.
        this.userListData.participants.push(movedParticipant);

        this.userListData.filteredParticipants.sort((a, b) => a.lastname.localeCompare(b.lastname));
        this.userListData.participants.sort((a, b) => a.lastname.localeCompare(b.lastname));

      }
    }
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

  dropMember(event: CdkDragDrop<(Participant | undefined)[], any>, team: Team, memberIndex: number) {
    const sourceTeam: Team | undefined = this.searchTeam(event);
    const participant = event.item.data;
    team.members = this.getMembersContainer(team);
    if (sourceTeam === team) {
      //Reordering the team.
      const sourceIndex: number | undefined = this.members.get(sourceTeam)?.indexOf(event.item.data);
      if (sourceIndex || sourceIndex === 0) {
        //Moving to an empty space.
        // if (team.members[memberIndex] == undefined) {
        //   team.members[1] = undefined;
        // } else {
        //   //Swapping with an existing member.
        //   moveItemInArray(team.members, sourceIndex, memberIndex);
        // }
        moveItemInArray(team.members, sourceIndex, memberIndex);
      }
      this.updateTeam(team, participant);
    } else {
      //Move from user list to team.
      this.transferCard(event, memberIndex);
      // Update origin team.
      if (sourceTeam) {
        //Delete member from team, as it is returning to the user list.
        this.updateTeam(sourceTeam, undefined);
      }
      //Updated destination team.
      this.updateTeam(team, participant);
    }
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
    this.teamService.update(team).pipe(
      tap((newTeam: Team) => {
        member ? this.loggerService.info("Team '" + newTeam.name + "' member '" + member.name + " " + member.lastname + "' updated.") :
          this.loggerService.info("Team '" + newTeam.name + "' updated.");
      }),
      catchError(member ? this.messageService.handleError<Team>("Updating '" + member.name + " " + member.lastname + "'") :
        this.messageService.handleError<Team>("Updating '" + team.name + "'"))
    ).subscribe(() => member ? this.messageService.infoMessage("Team '" + Team.name + "' member '" + member.name + " " + member.lastname + "' updated.") : "");
  }

  searchTeam(event: CdkDragDrop<(Participant | undefined)[], any>) {
    const participant: Participant = event.previousContainer.data[event.previousIndex];
    for (let team of [...this.members.keys()]) {
      if (this.getMembersContainer(team).indexOf(participant) !== -1) {
        return team;
      }
    }
    return undefined;
  }

  checkTeamSize(_item: CdkDrag, dropList: CdkDropList): boolean {
    const size = dropList.element.nativeElement.getAttribute('data-tournament-size');
    if (!!size) {
      return (dropList.data.length < +size);
    }
    return true;
  }

  setEditable(team: Team, editable: boolean) {
    if (this.tournament.teamSize > 1) {
      team.editing = editable;
    }
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
      this.members.set(_team, []);
    });
  }

  deleteTeam(team: Team): void {
    for (let participant of team.members) {
      if (participant) {
        if (this.userListData.participants.indexOf(participant) < 0) {
          this.userListData.participants.push(participant);
        }
        if (this.userListData.filteredParticipants.indexOf(participant) < 0) {
          this.userListData.filteredParticipants.push(participant);
        }
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

  randomTeams(): void {
    let participants: Participant[];
    participants = [...Array.prototype.concat.apply([], [...this.members.values()]), ...this.userListData.participants];
    for (let team of this.teams) {
      team.members = [];
      for (let i = 0; i < (this.tournament.teamSize ? this.tournament.teamSize : 1); i++) {
        const participant: Participant = this.getRandomMember(participants);
        if (participant) {
          team.members[i] = participant;
        }
      }
      this.members.set(team, team.members);
      this.teamService.update(team).pipe(
        tap((newTeam: Team) => {
          this.loggerService.info("Team '" + newTeam.name + "' updated.");
        }),
        catchError(this.messageService.handleError<Team>("Updating '" + team.name + "'"))
      ).subscribe();
    }
    //Remaining one on left column.
    this.userListData.participants = participants;
    this.userListData.filteredParticipants = this.userListData.participants;
  }

  getRandomMember(participants: Participant[]): Participant {
    const selected: number = Math.floor(Math.random() * participants.length);
    const participant: Participant = participants[selected];
    participants.splice(selected, 1);
    return participant;
  }

  generateTeams() {
    if (this.tournament.teamSize === 1) {
      this.teams = [];
      let participants: Participant[];
      participants = [...Array.prototype.concat.apply([], [...this.members.values()]), ...this.userListData.participants];
      this.members = new Map<Team, Participant[]>();
      for (const member of participants) {
        const team: Team = new Team();
        team.tournament = this.tournament;
        team.name = this.nameUtilsService.getLastnameName(member);
        team.members = [];
        team.members[0] = member;
        this.teams.push(team);
      }
      this.teamService.setAll(this.teams).subscribe(_teams => {
        this.messageService.infoMessage(_teams.length + " team added!");
        this.teams = _teams
        this.userListData.participants = [];
        this.userListData.filteredParticipants = this.userListData.participants;
        for (const team of _teams) {
          this.members.set(team, team.members);
        }
        this.systemOverloadService.isBusy.next(false);
      });
    }
  }
}
