import {Component, Input, OnInit} from '@angular/core';
import {Group} from "../../models/group";
import {CdkDragDrop, transferArrayItem} from "@angular/cdk/drag-drop";
import {Team} from "../../models/team";
import {TeamListData} from "../basic/team-list/team-list-data";
import {TeamService} from "../../services/team.service";
import {Tournament} from "../../models/tournament";

@Component({
  selector: 'app-tournament-brackets-editor',
  templateUrl: './tournament-brackets-editor.component.html',
  styleUrls: ['./tournament-brackets-editor.component.scss']
})
export class TournamentBracketsEditorComponent implements OnInit {

  @Input()
  tournament: Tournament;
  @Input()
  groups: Group[];
  @Input()
  relations: Map<number, { src: number, dest: number }[]>;

  teamListData: TeamListData = new TeamListData();
  teamsOrder: Team[] = [];

  constructor(private teamService: TeamService) {
  }

  ngOnInit(): void {
    // this.teamService.getFromTournament(this.tournament).subscribe((teams: Team[]): void => {
    //   if (teams) {
    //     teams.sort(function (a: Team, b: Team) {
    //       return a.name.localeCompare(b.name);
    //     });
    //   }
    //   this.teamListData.teams = teams;
    //   this.teamListData.filteredTeams = teams;
    // });
    const teams : Team[] = [];
    teams.push(new Team("Team1"));
    teams.push(new Team("Team2"));
    teams.push(new Team("Team3"));
    this.teamListData.teams = teams;
    this.teamListData.filteredTeams = teams;
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
