import {Component, Inject, OnInit, Optional} from '@angular/core';
import {FightDialogBoxComponent} from "../fight-dialog-box/fight-dialog-box.component";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {TeamService} from "../../../services/team.service";
import {FightService} from "../../../services/fight.service";
import {GroupService} from "../../../services/group.service";
import {MessageService} from "../../../services/message.service";
import {GroupUpdatedService} from "../../../services/notifications/group-updated.service";
import {Action} from "../../../action";
import {Fight} from "../../../models/fight";
import {Group} from "../../../models/group";
import {Tournament} from "../../../models/tournament";
import {Team} from "../../../models/team";
import {CdkDragDrop, transferArrayItem} from "@angular/cdk/drag-drop";
import {forkJoin, Observable} from "rxjs";
import {TournamentExtendedProperty} from "../../../models/tournament-extended-property.model";
import {TournamentExtraPropertyKey} from "../../../models/tournament-extra-property-key";
import {TournamentExtendedPropertiesService} from "../../../services/tournament-extended-properties.service";

@Component({
  selector: 'app-senbatsu-fight-dialog-box',
  templateUrl: './senbatsu-fight-dialog-box.component.html',
  styleUrls: ['./senbatsu-fight-dialog-box.component.scss']
})
export class SenbatsuFightDialogBoxComponent extends FightDialogBoxComponent implements OnInit {

  private originalTeams: Team[];
  public teamDragDisabled: Team[];

  constructor(
    public override dialogRef: MatDialogRef<FightDialogBoxComponent>,
    protected override teamService: TeamService,
    protected override fightService: FightService,
    protected override groupServices: GroupService,
    protected override messageService: MessageService,
    protected override groupUpdatedService: GroupUpdatedService,
    @Optional() @Inject(MAT_DIALOG_DATA) public override data: {
      action: Action,
      entity: Fight,
      group: Group,
      previousFight: Fight | undefined,
      tournament: Tournament,
      swappedColors: boolean,
      swappedTeams: boolean,
      horizontalTeams: boolean,
      grid: boolean,
    },
    private tournamentExtendedPropertiesService: TournamentExtendedPropertiesService
  ) {
    super(dialogRef, teamService, fightService, groupServices, messageService, groupUpdatedService, data);
  }


  override getTeams(): void {
    const teamRequest: Observable<Team[]> = this.teamService.getRemainingFromTournament(this.tournament);
    const challengeRangeRequest: Observable<TournamentExtendedProperty> = this.tournamentExtendedPropertiesService.getByTournamentAndKey(this.tournament, TournamentExtraPropertyKey.SENBATSU_CHALLENGE_DISTANCE);

    forkJoin([teamRequest, challengeRangeRequest]).subscribe(([_teams, _challengeProperty]): void => {
      //Set the teams.
      this.originalTeams = [..._teams];
      const challengerTeam: Team | undefined = _teams.shift();
      if (challengerTeam !== undefined) {
        this.selectedTeam1.push(challengerTeam);
      }
      this.teamListData.teams = _teams;
      this.teamListData.filteredTeams = _teams;

      //Disable teams that are not reachable.
      const challengeDistance: number = Number(_challengeProperty.propertyValue);
      console.log(challengeDistance)
      this.teamDragDisabled = _teams.slice(challengeDistance);
    });

  }

  override dropTeam(event: CdkDragDrop<Team[], any>): Team {
    if (event.container.data.length === 0 || (event.container.data !== this.selectedTeam1 || event.container.data !== this.selectedTeam2)) {
      transferArrayItem(
        event.previousContainer.data,
        event.container.data,
        event.previousIndex,
        event.currentIndex,
      );
    }
    this.teamListData.filteredTeams = this.teamListData.filteredTeams.sort((a: Team, b: Team) => this.originalTeams.indexOf(a) - this.originalTeams.indexOf(b));
    this.teamListData.teams = this.teamListData.teams.sort((a: Team, b: Team) => this.originalTeams.indexOf(a) - this.originalTeams.indexOf(b));
    return event.container.data[event.currentIndex];
  }
}
