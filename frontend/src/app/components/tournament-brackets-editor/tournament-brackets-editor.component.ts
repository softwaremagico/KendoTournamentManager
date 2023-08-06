import {Component, Input, OnChanges, SimpleChanges} from '@angular/core';
import {Group} from "../../models/group";
import {CdkDragDrop, transferArrayItem} from "@angular/cdk/drag-drop";
import {Team} from "../../models/team";
import {TeamListData} from "../basic/team-list/team-list-data";
import {TeamService} from "../../services/team.service";
import {Tournament} from "../../models/tournament";
import {GroupLink} from "../../models/group-link.model";
import {GroupLinkService} from "../../services/group-link.service";
import {GroupService} from "../../services/group.service";

@Component({
  selector: 'app-tournament-brackets-editor',
  templateUrl: './tournament-brackets-editor.component.html',
  styleUrls: ['./tournament-brackets-editor.component.scss']
})
export class TournamentBracketsEditorComponent implements OnChanges {

  @Input()
  tournament: Tournament;

  groups: Group[];

  //Level -> Src Group -> Dst Group
  relations: Map<number, { src: number, dest: number }[]>;

  teamListData: TeamListData = new TeamListData();

  totalTeams: number;

  constructor(private teamService: TeamService, private groupService: GroupService, private groupLinkService: GroupLinkService) {
  }

  ngOnChanges(changes: SimpleChanges): void {
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
      })
      this.groupLinkService.getFromTournament(this.tournament.id!).subscribe((_groupRelations: GroupLink[]): void => {
        this.relations = this.convert(_groupRelations);
      })
    }
  }


  convert(groupRelations: GroupLink[]): Map<number, { src: number, dest: number }[]> {
    const relations: Map<number, { src: number, dest: number }[]> = new Map();
    for (const groupLink of groupRelations) {
      if (!relations.get(groupLink.source!.level!)) {
        relations.set(groupLink.source!.level!, []);
      }
      relations.get(groupLink.source!.level!)?.push({src: groupLink.source!.id!, dest: groupLink.destination!.id!});
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

}
