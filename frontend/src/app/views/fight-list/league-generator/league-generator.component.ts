import {Component, Inject, OnInit, Optional} from '@angular/core';
import {Action} from "../../../action";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {Tournament} from "../../../models/tournament";
import {teamListData} from "../../../components/basic/team-list/team-list-data";
import {TeamService} from "../../../services/team.service";
import {CdkDragDrop, transferArrayItem} from "@angular/cdk/drag-drop";
import {Team} from "../../../models/team";

@Component({
  selector: 'app-league-generator',
  templateUrl: './league-generator.component.html',
  styleUrls: ['./league-generator.component.scss']
})
export class LeagueGeneratorComponent implements OnInit {

  teamListData: teamListData = new teamListData();
  title: string;
  action: Action;
  actionName: string;
  teamsSorted: Team[] = [];

  tournament: Tournament;

  constructor(public dialogRef: MatDialogRef<LeagueGeneratorComponent>,
              private teamService: TeamService,
              @Optional() @Inject(MAT_DIALOG_DATA) public data: { title: string, action: Action, tournament: Tournament }) {
    this.title = data.title;
    this.action = data.action;
    this.actionName = Action[data.action];
    this.tournament = data.tournament;
  }

  ngOnInit(): void {
    this.teamService.getFromTournament(this.tournament).subscribe(teams => {
      teams.sort(function (a, b) {
        return a.name.localeCompare(b.name);
      });
      console.log(teams)
      this.teamListData.teams = teams;
      this.teamListData.filteredTeams = teams;
    });
  }

  doAction() {
    this.dialogRef.close({data: this.teamsSorted, action: this.action});
  }

  closeDialog() {
    this.dialogRef.close({action: Action.Cancel});
  }

  private transferCard(event: CdkDragDrop<Team[], any>): Team {
    transferArrayItem(
      event.previousContainer.data,
      event.container.data,
      event.previousIndex,
      event.currentIndex,
    );
    return event.container.data[event.currentIndex];
  }

  removeTeam(event: CdkDragDrop<Team[], any>) {
    transferArrayItem(
      event.previousContainer.data,
      event.container.data,
      event.previousIndex,
      event.currentIndex,
    );
    // const team: Team = event.container.data[event.currentIndex];
    this.teamListData.filteredTeams.sort((a, b) => a.name.localeCompare(b.name));
    this.teamListData.teams.sort((a, b) => a.name.localeCompare(b.name));
  }

  dropTeam(event: CdkDragDrop<Team[], any>) {
    const team: Team = this.transferCard(event);
    if (this.teamListData.teams.includes(team)) {
      this.teamListData.teams.splice(this.teamListData.teams.indexOf(team), 1);
    }
    if (this.teamListData.filteredTeams.includes(team)) {
      this.teamListData.filteredTeams.splice(this.teamListData.filteredTeams.indexOf(team), 1);
    }
  }

}
