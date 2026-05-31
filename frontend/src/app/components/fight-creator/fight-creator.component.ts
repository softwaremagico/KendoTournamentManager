import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {Fight} from "../../models/fight";
import {TeamListData} from "../basic/team-list/team-list-data";
import {TeamService} from "../../services/team.service";
import {Tournament} from "../../models/tournament";
import {CdkDrag, CdkDragDrop, CdkDropList, transferArrayItem} from "@angular/cdk/drag-drop";
import {Team} from "../../models/team";
import {GroupService} from "../../services/group.service";
import {Group} from "../../models/group";
import {MessageService} from "../../services/message.service";
import {FightService} from "../../services/fight.service";
import {GroupUpdatedService} from "../../services/notifications/group-updated.service";

@Component({
  selector: 'fight-creator',
  templateUrl: './fight-creator.component.html',
  styleUrls: ['./fight-creator.component.scss']
})
export class FightCreator implements OnInit {

  @Input()
  tournament: Tournament;
  @Input()
  previousFight: Fight | undefined;
  @Input()
  fight: Fight;
  @Input()
  group: Group;
  @Input()
  swappedColors: boolean = false;
  @Input()
  swappedTeams: boolean = false;
  @Input()
  horizontalTeams: boolean = false;
  @Input()
  grid: boolean = false;
  @Output()
  onClosed: EventEmitter<void> = new EventEmitter<void>();

  teamListData: TeamListData = new TeamListData();

  selectedTeam1: Team[] = [];
  selectedTeam2: Team[] = [];

  constructor(
    protected teamService: TeamService, protected fightService: FightService, protected groupServices: GroupService,
    protected messageService: MessageService, protected groupUpdatedService: GroupUpdatedService) {
  }

  ngOnInit(): void {
    this.getTeams();
  }

  getTeams(): void {
    this.teamService.getRemainingFromTournament(this.tournament).subscribe((_teams: Team[]): void => {
      _teams.sort(function (a: Team, b: Team) {
        return a.name.localeCompare(b.name);
      });
      this.teamListData.teams = _teams;
      this.teamListData.filteredTeams = _teams;
    });
  }

  closeDialog(): void {
    this.onClosed.emit();
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
        this.closeDialog();
      });
    });


  }

}
