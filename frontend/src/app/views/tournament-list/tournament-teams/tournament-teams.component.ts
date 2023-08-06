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
import {SystemOverloadService} from "../../../services/notifications/system-overload.service";
import {Club} from "../../../models/club";
import {RbacBasedComponent} from "../../../components/RbacBasedComponent";
import {RbacService} from "../../../services/rbac/rbac.service";
import {GroupService} from "../../../services/group.service";
import {Group} from "../../../models/group";
import {StatisticsChangedService} from "../../../services/notifications/statistics-changed.service";
import {FightService} from "../../../services/fight.service";
import {RankingService} from "../../../services/ranking.service";
import {random} from "../../../utils/random/random";
import {FilterResetService} from "../../../services/notifications/filter-reset.service";
import {Fight} from "../../../models/fight";
import {Role} from "../../../models/role";

@Component({
  selector: 'app-tournament-teams',
  templateUrl: './tournament-teams.component.html',
  styleUrls: ['./tournament-teams.component.scss']
})
export class TournamentTeamsComponent extends RbacBasedComponent implements OnInit {

  userListData: UserListData = new UserListData();
  tournament: Tournament;
  teams: Team[];
  members: Map<Team, (Participant | undefined)[]> = new Map<Team, (Participant | undefined)[]>();
  groups: Group[];
  teamSize: number[];

  constructor(public dialogRef: MatDialogRef<TournamentTeamsComponent>, private messageService: MessageService,
              private loggerService: LoggerService, private teamService: TeamService, private roleService: RoleService,
              public nameUtilsService: NameUtilsService, private systemOverloadService: SystemOverloadService,
              rbacService: RbacService, private groupService: GroupService, private fightService: FightService,
              private rankingService: RankingService, private statisticsChangedService: StatisticsChangedService,
              private filterResetService: FilterResetService,
              @Optional() @Inject(MAT_DIALOG_DATA) public data: { tournament: Tournament }) {
    super(rbacService);
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
      this.userListData.participants = roles.map((role: Role) => role.participant);
      //Block participants.
      if (this.tournament.locked) {
        for (let participant of this.userListData.participants) {
          participant.locked = participant.locked || this.tournament.locked;
        }
      }
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
        this.teamSize = []
        for (let i = 0; i < this.tournament.teamSize; i++) {
          this.teamSize.push(i);
        }
      }
    });
    //Get tournament groups
    this.groupService.getFromTournament(this.tournament.id!).subscribe((_groups: Group[]): void => {
        this.groups = _groups;
      }
    )
    //Prevent removing teams that are on fights
    this.fightService.getFromTournament(this.tournament).subscribe((_fights: Fight[]): void => {
      let teamInFights: Team[] = [];
      teamInFights.push(..._fights.map(fight => fight.team1));
      teamInFights.push(..._fights.map(fight => fight.team2));
      //Remove duplicates.
      teamInFights = teamInFights.filter((team: Team, i: number, a: Team[]): boolean => i === a.indexOf(team));
      if (this.teams) {
        for (let team of this.teams) {
          team.locked = teamInFights.some(t => t.id === team.id);
        }
      }
    })
  }

  @HostListener('document:click', ['$event.target'])
  onClick(element: HTMLElement): void {
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

  closeDialog(): void {
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

  removeFromTeam(event: CdkDragDrop<Participant[], any>): void {
    // Correct index, as always return the first non-empty.
    const sourceTeam: Team | undefined = this.searchTeam(event as CdkDragDrop<(Participant | undefined)[], any>);
    const movedParticipant: Participant = event.item.data;

    //Remove from source data.
    if (sourceTeam && this.members) {
      const sourceIndex: number | undefined = this.members.get(sourceTeam)?.indexOf(movedParticipant);
      if (sourceIndex || sourceIndex === 0) {
        //Removing team member from team.
        const teamMembers: (Participant | undefined)[] | undefined = this.members.get(sourceTeam);
        if (teamMembers) {
          teamMembers[sourceIndex] = undefined;
          this.members.set(sourceTeam, teamMembers);
        }

        this.deleteMemberFromTeam(movedParticipant);
        this.updateTeam(sourceTeam, undefined);
        //Add to user list.
        this.userListData.participants.push(movedParticipant);
        this.userListData.filteredParticipants.push(movedParticipant);

        this.userListData.filteredParticipants.sort((a: Participant, b: Participant) => a.lastname.localeCompare(b.lastname));
        this.userListData.participants.sort((a: Participant, b: Participant) => a.lastname.localeCompare(b.lastname));
      }
    }
  }

  deleteMemberFromTeam(participant: Participant): void {
    this.teamService.deleteByMemberAndTournament(participant, this.tournament).pipe(
      tap(() => {
        this.loggerService.info("infoMemberDeleted");
      }),
      catchError(this.messageService.handleError<Team>("removing '" + participant.name + " " + participant.lastname + "'"))
    ).subscribe(() => {
      this.messageService.infoMessage("infoMemberDeleted");
    });
  }

  dropMember(event: CdkDragDrop<(Participant | undefined)[], any>, team: Team, memberIndex: number): void {
    const sourceTeam: Team | undefined = this.searchTeam(event);
    const participant = event.item.data;
    team.members = this.getMembersContainer(team);
    if (sourceTeam === team) {
      //Reordering the team.
      const sourceIndex: number | undefined = this.members.get(sourceTeam)?.indexOf(event.item.data);
      if (sourceIndex || sourceIndex === 0) {
        // Moving to an empty space.
        if (team.members[memberIndex] == undefined) {
          team.members[memberIndex] = participant;
          team.members[sourceIndex] = undefined;
        } else {
          //Swapping with an existing member.
          moveItemInArray(team.members, sourceIndex, memberIndex);
        }
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
    //Reset filter
    this.filterResetService.resetFilter.next(true);
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

  updateTeam(team: Team, member: Participant | undefined): void {
    this.teamService.update(team).pipe(
      tap((newTeam: Team) => {
        if (member) {
          this.loggerService.info("Team '" + newTeam.name + "' member '" + member.name + " " + member.lastname + "' updated.")
        } else {
          this.loggerService.info("Team '" + newTeam.name + "' updated.");
        }
      }),
      catchError(member ? this.messageService.handleError<Team>("Updating '" + member.name + " " + member.lastname + "'") :
        this.messageService.handleError<Team>("Updating '" + team.name + "'"))
    ).subscribe(() => {
      if (member) {
        this.messageService.infoMessage("infoTeamUpdated");
      }
      this.statisticsChangedService.areStatisticsChanged.next(true);
    })
    ;
  }

  searchTeam(event: CdkDragDrop<(Participant | undefined)[], any>): Team | undefined {
    const participant: Participant = event.previousContainer.data[event.previousIndex];
    for (let team of [...this.members.keys()]) {
      if (this.getMembersContainer(team).includes(participant)) {
        return team;
      }
    }
    return undefined;
  }

  dropListEnterPredicate(memberIndex: number, team: Team) {
    return function (_item: CdkDrag<Participant>, dropList: CdkDropList): boolean {
      if (team) {
        return !team.locked && (team.members[memberIndex] === undefined || team.members[memberIndex] === null);
      }
      return true;
    };
  }

  isTeamLocked(team: Team): boolean {
    return team.locked;
  }

  setEditable(team: Team, editable: boolean): void {
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
    ).subscribe((): void => {
      this.messageService.infoMessage("infoTeamUpdated");
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
      const teams: Team[] = [];
      teams.push(_team);
      this.groupService.addTeamsToGroup(this.groups[0]!.id!, teams).pipe(
        tap(() => {
          this.loggerService.info("Adding team to group.");
        }),
        catchError(this.messageService.handleError<Group>("Adding team to group."))
      ).subscribe(() => {
        this.messageService.infoMessage("infoTeamStored");
        this.teams.push(_team);
        this.members.set(_team, []);
      });
      this.statisticsChangedService.areStatisticsChanged.next(true);
    });
  }

  deleteTeam(team: Team): void {
    for (let participant of team.members) {
      if (participant) {
        if (!this.userListData.participants.includes(participant)) {
          this.userListData.participants.push(participant);
        }
        if (!this.userListData.filteredParticipants.includes(participant)) {
          this.userListData.filteredParticipants.push(participant);
        }
      }
    }
    this.userListData.filteredParticipants.sort((a, b) => a.lastname.localeCompare(b.lastname));
    this.userListData.participants.sort((a, b) => a.lastname.localeCompare(b.lastname));

    const teams: Team[] = [];
    teams.push(team);
    this.groupService.deleteTeamsFromGroup(this.groups[0]!.id!, teams).pipe(
      tap((): void => {
        this.loggerService.info("Team '" + team.name + "' removed from group.");
      }),
      catchError(this.messageService.handleError<Team>("removing team '" + team.name + "' from group."))
    ).subscribe(() => {
      this.teamService.delete(team).pipe(
        tap(() => {
          this.loggerService.info("Team '" + team.name + "' removed.");
        }),
        catchError(this.messageService.handleError<Team>("removing team '" + team.name + "' from database."))
      ).subscribe(() => {
        this.messageService.infoMessage("infoTeamDeleted");
        this.teams.splice(this.teams.indexOf(team), 1);
        this.statisticsChangedService.areStatisticsChanged.next(true);
      });
    });
    this.members.delete(team);
  }

  balancedTeams(): void {
    let participants: Participant[];
    participants = [...Array.prototype.concat.apply([], [...this.members.values()]), ...this.userListData.participants];

    this.rankingService.getCompetitorsGlobalScoreRanking(participants).subscribe(_scoreRanking => {
      for (let team of this.teams) {
        team.members = [];
        for (let i = 0; i < (this.tournament.teamSize ? this.tournament.teamSize : 1); i++) {
          const participant: Participant = this.getBalancedMember(participants, team.members.length,
            (this.tournament.teamSize ? this.tournament.teamSize : 1));
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
        ).subscribe(() => this.statisticsChangedService.areStatisticsChanged.next(true));
      }
      //Remaining one on left column.
      this.userListData.participants = participants;
      this.userListData.filteredParticipants = this.userListData.participants;
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
      ).subscribe(() => this.statisticsChangedService.areStatisticsChanged.next(true));
    }
    //Remaining one on left column.
    this.userListData.participants = participants;
    this.userListData.filteredParticipants = this.userListData.participants;
  }

  getRandomMember(participants: Participant[]): Participant {
    const selected: number = Math.floor(random() * participants.length);
    const participant: Participant = participants[selected];
    participants.splice(selected, 1);
    return participant;
  }

  getBalancedMember(participants: Participant[], selectFromSector: number, availableSectors: number): Participant {
    let selected: number = Math.floor(random() * (participants.length / availableSectors));
    let participant: Participant;
    if (selectFromSector == 0) {
      participant = participants[selected];
      participants.splice(selected, 1);
    } else if (selectFromSector == availableSectors - 1) {
      selected = participants.length - selected - 1;
      participant = participants[selected];
      participants.splice(selected, 1);
    } else {
      selected = Math.floor((participants.length / availableSectors)) * selectFromSector + selected;
      participant = participants[selected];
      participants.splice(selected, 1);
    }
    return participant;
  }

  generateTeams(): void {
    if (this.tournament.teamSize === 1) {
      this.assignTeamByParticipant();
    } else {
      this.balancedTeams();
    }
  }

  assignTeamByParticipant(): void {
    this.teams = [];
    let participants: Participant[];
    participants = [...Array.prototype.concat.apply([], [...this.members.values()]), ...this.userListData.participants];
    this.members = new Map<Team, Participant[]>();
    for (const member of participants) {
      const team: Team = new Team();
      team.tournament = this.tournament;
      team.name = this.nameUtilsService.getLastnameNameNoSpaces(member);
      team.members = [];
      team.members[0] = member;
      this.teams.push(team);
    }
    this.teamService.setAll(this.teams).subscribe(_teams => {
      this.messageService.infoMessage("infoTeamsAdded");
      this.teams = _teams
      this.userListData.participants = [];
      this.userListData.filteredParticipants = this.userListData.participants;
      for (const team of _teams) {
        this.members.set(team, team.members);
      }
      this.systemOverloadService.isBusy.next(false);
      this.statisticsChangedService.areStatisticsChanged.next(true);
    });
  }

  downloadPDF(): void {
    if (this.tournament?.id) {
      this.teamService.getTeamsByTournament(this.tournament.id).subscribe((pdf: Blob): void => {
        const blob: Blob = new Blob([pdf], {type: 'application/pdf'});
        const downloadURL: string = window.URL.createObjectURL(blob);

        const anchor: HTMLAnchorElement = document.createElement("a");
        anchor.download = "Team List - " + this.tournament.name + ".pdf";
        anchor.href = downloadURL;
        anchor.click();
      });
    }
  }
}
