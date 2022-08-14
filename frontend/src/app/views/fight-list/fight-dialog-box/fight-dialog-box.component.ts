import {Component, Inject, OnInit, Optional} from '@angular/core';
import {Fight} from "../../../models/fight";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {Action} from "../../../action";
import {TeamListData} from "../../../components/basic/team-list/team-list-data";
import {TeamService} from "../../../services/team.service";
import {Tournament} from "../../../models/tournament";
import {CdkDragDrop, transferArrayItem} from "@angular/cdk/drag-drop";
import {Team} from "../../../models/team";

@Component({
  selector: 'app-fight-dialog-box',
  templateUrl: './fight-dialog-box.component.html',
  styleUrls: ['./fight-dialog-box.component.scss']
})
export class FightDialogBoxComponent implements OnInit {

  teamListData: TeamListData = new TeamListData();
  tournament: Tournament;
  fight: Fight;
  title: string;
  action: Action;
  actionName: string;

  constructor(
    public dialogRef: MatDialogRef<FightDialogBoxComponent>,
    private teamService: TeamService,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: { title: string, action: Action, entity: Fight, tournament: Tournament }
  ) {
    this.fight = data.entity;
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
      this.teamListData.teams = teams;
      this.teamListData.filteredTeams = teams;
    });
  }

  closeDialog() {
    this.dialogRef.close();
  }

  removeTeam(event: CdkDragDrop<Team[], any>) {
    transferArrayItem(
      event.previousContainer.data,
      event.container.data,
      event.previousIndex,
      event.currentIndex,
    );
    // const team: Team = event.container.data[event.currentIndex]
    // this.roleService.deleteByParticipantAndTournament(participant, this.tournament).subscribe(() => {
    //   this.messageService.infoMessage("Role for '" + participant.name + " " + participant.lastname + "' removed.");
    // });
    this.teamListData.filteredTeams.sort((a, b) => a.name.localeCompare(b.name));
    this.teamListData.teams.sort((a, b) => a.name.localeCompare(b.name));
  }

}
