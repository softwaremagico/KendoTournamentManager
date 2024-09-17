import {Component, Inject, OnInit, Optional} from '@angular/core';
import {Fight} from "../../../models/fight";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {Action} from "../../../action";
import {TeamListData} from "../../../components/basic/team-list/team-list-data";
import {TeamService} from "../../../services/team.service";
import {Tournament} from "../../../models/tournament";
import {CdkDrag, CdkDragDrop, CdkDropList, transferArrayItem} from "@angular/cdk/drag-drop";
import {Team} from "../../../models/team";
import {GroupService} from "../../../services/group.service";
import {Group} from "../../../models/group";
import {MessageService} from "../../../services/message.service";
import {FightService} from "../../../services/fight.service";
import {GroupUpdatedService} from "../../../services/notifications/group-updated.service";

@Component({
  selector: 'app-fight-dialog-box',
  templateUrl: './fight-dialog-box.component.html',
  styleUrls: ['./fight-dialog-box.component.scss']
})
export class FightDialogBoxComponent implements OnInit {

  teamListData: TeamListData = new TeamListData();
  tournament: Tournament;
  previousFight: Fight | undefined;
  fight: Fight;
  group: Group;
  title: string;
  action: Action;
  actionName: string;

  swappedColors: boolean = false;
  swappedTeams: boolean = false;
  horizontalTeams: boolean = false;
  grid: boolean = false;

  selectedTeam1: Team[] = [];
  selectedTeam2: Team[] = [];

  constructor(
    public dialogRef: MatDialogRef<FightDialogBoxComponent>,
    private teamService: TeamService,
    private fightService: FightService,
    private groupServices: GroupService,
    private messageService: MessageService,
    private groupUpdatedService: GroupUpdatedService,
    @Optional() @Inject(MAT_DIALOG_DATA) public data: {
      title: string,
      action: Action,
      entity: Fight,
      group: Group,
      previousFight: Fight | undefined,
      tournament: Tournament,
      swappedColors: boolean,
      swappedTeams: boolean,
      horizontalTeams: boolean,
      grid: boolean,
    }
  ) {
    this.group = data.group;
    this.previousFight = data.previousFight;
    this.fight = data.entity;
    this.title = data.title;
    this.action = data.action;
    this.actionName = Action[data.action];
    this.tournament = data.tournament;
    this.swappedColors = data.swappedColors;
    this.swappedTeams = data.swappedTeams;
    this.horizontalTeams = data.horizontalTeams;
    this.grid = data.grid;
  }

  ngOnInit(): void {
    this.teamService.getFromTournament(this.tournament).subscribe((teams: Team[]): void => {
      teams.sort(function (a: Team, b: Team) {
        return a.name.localeCompare(b.name);
      });
      this.teamListData.teams = teams;
      this.teamListData.filteredTeams = teams;
    });
  }

  closeDialog(): void {
    this.dialogRef.close();
  }

  dropTeam(event: CdkDragDrop<Team[], any>): Team {
    if (event.container.data.length === 0 || (event.container.data !== this.selectedTeam1 || event.container.data !== this.selectedTeam2)) {
      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex,
      );
    }
    this.teamListData.filteredTeams.sort((a: Team, b: Team) => a.name.localeCompare(b.name));
    this.teamListData.teams.sort((a: Team, b: Team) => a.name.localeCompare(b.name));
    return event.container.data[event.currentIndex];
  }

  checkDroppedElement(item: CdkDrag<Team>, drop: CdkDropList) {
    return (drop.data.length === 0 || drop.data.length === 1 && drop.data!.includes(item.data));
  }

  addFights(): void {
    this.fight.team1 = this.selectedTeam1[0];
    this.fight.team2 = this.selectedTeam2[0];

    this.fightService.generateDuels(this.fight).subscribe((_fight: Fight): void => {
      if (this.previousFight !== undefined) {
        this.group.fights.splice(this.group.fights.findIndex((fight: Fight): boolean => this.previousFight?.id === fight.id
        ) + 1, 0, _fight);
      } else if (!this.group.fights.includes(_fight)) {
        this.group.fights.push(_fight);
      }

      this.groupServices.update(this.group).subscribe((_group: Group): void => {
        this.messageService.infoMessage("addFightMessage");
        this.groupUpdatedService.isGroupUpdated.next(_group);
        this.dialogRef.close(this.fight);
      });
    });


  }

}
