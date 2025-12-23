import {Component, EventEmitter, Inject, Input, OnInit, Optional, Output} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {ParticipantService} from "../../../services/participant.service";
import {Tournament} from "../../../models/tournament";
import {UserListData} from "../../../components/basic/user-list/user-list-data";
import {CdkDragDrop, transferArrayItem} from "@angular/cdk/drag-drop";
import {Participant} from "../../../models/participant";
import {RoleType} from "../../../models/role-type";
import {RoleService} from "../../../services/role.service";
import {MessageService} from "../../../services/message.service";
import {Role} from "../../../models/role";
import {forkJoin} from "rxjs";
import {TranslocoService} from "@ngneat/transloco";
import {RbacService} from "../../../services/rbac/rbac.service";
import {RbacBasedComponent} from "../../../components/RbacBasedComponent";
import {FilterResetService} from "../../../services/notifications/filter-reset.service";
import {StatisticsChangedService} from "../../../services/notifications/statistics-changed.service";
import {TeamService} from "../../../services/team.service";
import {Team} from "../../../models/team";

@Component({
  selector: 'tournament-roles',
  templateUrl: './tournament-roles.component.html',
  styleUrls: ['./tournament-roles.component.scss']
})
export class TournamentRolesComponent extends RbacBasedComponent implements OnInit {

  @Input()
  tournament: Tournament;
  @Output() onClosed: EventEmitter<Tournament> = new EventEmitter<Tournament>();

  userListData: UserListData = new UserListData();
  roleTypes: RoleType[] = RoleType.toArray();
  participants: Map<RoleType, Participant[]> = new Map<RoleType, Participant[]>();
  showAvatars: boolean = false;

  constructor(public participantService: ParticipantService, public roleService: RoleService,
              private messageService: MessageService,
              rbacService: RbacService, private filterResetService: FilterResetService,
              private statisticsChangedService: StatisticsChangedService,
              private teamService: TeamService) {
    super(rbacService);
  }

  getParticipantsContainer(role: RoleType): Participant[] {
    if (this.participants.get(role) === undefined) {
      this.participants.set(role, []);
    }
    return this.participants.get(role)!;
  }

  ngOnInit(): void {
    let participantsRequest = this.participantService.getAll();
    let roleRequests = this.roleService.getFromTournamentAndTypes(this.tournament.id!, RoleType.toArray());

    forkJoin([participantsRequest, roleRequests]).subscribe(([participants, roles]): void => {
      //Get roles
      for (let roleType of this.roleTypes) {
        const rolesFromType: Role[] = roles.filter(role => role.roleType === roleType);
        this.participants.set(roleType, rolesFromType.map(role => role.participant).sort(function (a: Participant, b: Participant) {
          return a.lastname.localeCompare(b.lastname) || a.name.localeCompare(b.name);
        }));
        //Get participants and subtract the ones already with roles.
        const participantsIds: (number | undefined)[] = this.participants.get(roleType)!.map(participant => participant.id);
        participants = participants.filter((participantWithoutRole: Participant) => !participantsIds
          .includes(participantWithoutRole.id));
      }
      participants.sort(function (a: Participant, b: Participant) {
        return a.lastname.localeCompare(b.lastname) || a.name.localeCompare(b.name);
      });
      //Block participants and avatars.
      for (let participant of participants) {
        if (this.tournament.locked) {
          participant.locked = participant.locked || this.tournament.locked;
        }
        if (participant?.hasAvatar) {
          this.showAvatars = true;
        }
      }
      this.userListData.participants = participants;
      this.userListData.filteredParticipants = participants;
      //Prevent removing participants that are on teams already
      this.teamService.getFromTournament(this.tournament).subscribe((_teams: Team[]): void => {
        let teamMembers: Participant[] = [];
        for (let team of _teams) {
          for (let member of team.members) {
            if (member) {
              teamMembers.push(member);
            }
          }
        }
        for (let participant of this.participants.get(RoleType.COMPETITOR)!) {
          participant.locked = teamMembers.some(p => p.id === participant.id) || this.tournament.locked;
        }
      });
    });
  }

  transferCard(event: CdkDragDrop<Participant[], any>): Participant | undefined {
    if (event.previousContainer !== event.container) {
      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex,
      );
      return event.container.data[event.currentIndex];
    }
    return undefined;
  }

  removeRole(event: CdkDragDrop<Participant[], any>): void {
    transferArrayItem(
      event.previousContainer.data,
      event.container.data,
      event.previousIndex,
      event.currentIndex,
    );
    const participant: Participant = event.container.data[event.currentIndex]
    this.roleService.deleteByParticipantAndTournament(participant, this.tournament).subscribe((): void => {
      this.messageService.infoMessage("infoRoleDeleted");
      this.statisticsChangedService.areStatisticsChanged.next(true);
    });
    if (!this.userListData.filteredParticipants.includes(participant)) {
      this.userListData.filteredParticipants.push(participant);
    }
    if (!this.userListData.participants.includes(participant)) {
      this.userListData.participants.push(participant);
    }
    this.userListData.filteredParticipants.sort((a: Participant, b: Participant) => a.lastname.localeCompare(b.lastname));
    this.userListData.participants.sort((a: Participant, b: Participant) => a.lastname.localeCompare(b.lastname));
  }

  dropParticipant(event: CdkDragDrop<Participant[], any>, roleName: RoleType): void {
    const participant: Participant | undefined = this.transferCard(event);
    if (!participant) {
      return;
    }
    const role: Role = new Role();
    role.tournament = this.tournament;
    role.participant = participant;
    role.roleType = roleName;
    this.roleService.add(role).subscribe((_role: Role): void => {
      this.messageService.infoMessage("infoRoleStored");
      this.filterResetService.resetFilter.next(true);
      this.statisticsChangedService.areStatisticsChanged.next(true);
    });
    if (this.userListData.participants.includes(participant)) {
      this.userListData.participants.splice(this.userListData.participants.indexOf(participant), 1);
    }
    if (this.userListData.filteredParticipants.includes(participant)) {
      this.userListData.filteredParticipants.splice(this.userListData.filteredParticipants.indexOf(participant), 1);
    }
    this.shortRoles();
  }

  private shortRoles(): void {
    this.participants.forEach(function (value: Participant[], key: RoleType): void {
      value.sort((a: Participant, b: Participant) => a.lastname.localeCompare(b.lastname));
    });
  }

  downloadPDF(): void {
    if (this.tournament?.id) {
      this.roleService.getRolesByTournament(this.tournament.id).subscribe((pdf: Blob): void => {
        const blob: Blob = new Blob([pdf], {type: 'application/pdf'});
        const downloadURL: string = window.URL.createObjectURL(blob);

        const anchor: HTMLAnchorElement = document.createElement("a");
        anchor.download = "Role List - " + this.tournament.name + ".pdf";
        anchor.href = downloadURL;
        anchor.click();
      });
    }
  }

  countRole(roleType: RoleType): number {
    if (!this.participants!.get(roleType)) {
      return 0;
    }
    return this.participants.get(roleType)!.length;
  }
}

