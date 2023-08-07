import {Component, EventEmitter, Input, OnChanges, Output, SimpleChanges} from '@angular/core';
import {Group} from "../../models/group";
import {CdkDragDrop, transferArrayItem} from "@angular/cdk/drag-drop";
import {Team} from "../../models/team";
import {TeamListData} from "../basic/team-list/team-list-data";
import {TeamService} from "../../services/team.service";
import {Tournament} from "../../models/tournament";
import {GroupLink} from "../../models/group-link.model";
import {GroupLinkService} from "../../services/group-link.service";
import {GroupService} from "../../services/group.service";
import {RbacActivity} from "../../services/rbac/rbac.activity";
import {RbacService} from "../../services/rbac/rbac.service";
import {SystemOverloadService} from "../../services/notifications/system-overload.service";
import {GroupsUpdatedService} from "../../services/notifications/groups-updated.service";

@Component({
  selector: 'app-tournament-brackets-editor',
  templateUrl: './tournament-brackets-editor.component.html',
  styleUrls: ['./tournament-brackets-editor.component.scss']
})
export class TournamentBracketsEditorComponent implements OnChanges {

  @Input()
  tournament: Tournament;

  @Output()
  onSelectedGroup: EventEmitter<Group> = new EventEmitter();

  groups: Group[];

  selectedGroup: Group;

  //Level -> Src Group -> Dst Group
  relations: Map<number, { src: number, dest: number }[]>;

  teamListData: TeamListData = new TeamListData();

  totalTeams: number;

  constructor(private teamService: TeamService, private groupService: GroupService, private groupLinkService: GroupLinkService,
              private rbacService: RbacService, private systemOverloadService: SystemOverloadService,
              private groupsUpdatedService: GroupsUpdatedService) {
  }

  ngOnChanges(changes: SimpleChanges): void {
    this.systemOverloadService.isBusy.next(true);
    if (changes['tournament'] && this.tournament != undefined) {
      this.teamService.getFromTournament(this.tournament).subscribe((teams: Team[]): void => {
        if (teams) {
          teams.sort(function (a: Team, b: Team) {
            return a.name.localeCompare(b.name);
          });
        }
        this.teamListData.teams = teams;
        this.teamListData.filteredTeams = teams;
      });

      this.groupService.getFromTournament(this.tournament.id!).subscribe((_groups: Group[]): void => {
        this.groups = _groups;
        this.groupsUpdatedService.areGroupsUpdated.next(_groups);
      });

      this.groupLinkService.getFromTournament(this.tournament.id!).subscribe((_groupRelations: GroupLink[]): void => {
        this.relations = this.convert(_groupRelations);
        this.groupsUpdatedService.areRelationsUpdated.next(this.convert(_groupRelations));
      });
    }
  }

  selectGroup(group: Group): void {
    if (this.rbacService.isAllowed(RbacActivity.SELECT_GROUP)) {
      this.selectedGroup = group;
      this.onSelectedGroup.emit(group);
    }
  }


  convert(groupRelations: GroupLink[]): Map<number, { src: number, dest: number }[]> {
    const relations: Map<number, { src: number, dest: number }[]> = new Map();
    if (groupRelations) {
      for (const groupLink of groupRelations) {
        if (!relations.get(groupLink.source!.level!)) {
          relations.set(groupLink.source!.level!, []);
        }
        relations.get(groupLink.source!.level!)?.push({src: groupLink.source!.id!, dest: groupLink.destination!.id!});
      }
    }
    return relations;
  }


  removeTeam(event: CdkDragDrop<Team[], any>): void {
    transferArrayItem(
      event.previousContainer.data,
      event.container.data,
      event.previousIndex,
      event.currentIndex,
    );
    this.teamListData.filteredTeams.sort((a: Team, b: Team) => a.name.localeCompare(b.name));
    this.teamListData.teams.sort((a: Team, b: Team) => a.name.localeCompare(b.name));
  }

  addGroup(): void {
    const group: Group = new Group();
    group.tournament = this.tournament;
    group.level = 0;
    group.index = this.groups.length;
    this.groupService.addGroup(group).subscribe((_group: Group): void => {
      //Refresh all groups, also other levels that can change.
      this.groupService.getFromTournament(this.tournament!.id!).subscribe((_groups: Group[]): void => {
        console.log(_groups)
        this.groups = _groups;
      });
    });
  }

  deleteGroup(group: Group | undefined): void {
    if (group) {
      this.groupService.deleteGroup(group).subscribe((): void => {
        //Refresh all groups, also other levels that can change.
        this.groupService.getFromTournament(this.tournament!.id!).subscribe((_groups: Group[]): void => {
          this.groups = _groups;
        });
      });
    }
  }

}
