import {Component, OnInit} from '@angular/core';
import {FightCreator} from "../../../components/fight-creator/fight-creator.component";
import {TeamService} from "../../../services/team.service";
import {FightService} from "../../../services/fight.service";
import {GroupService} from "../../../services/group.service";
import {MessageService} from "../../../services/message.service";
import {GroupUpdatedService} from "../../../services/notifications/group-updated.service";
import {Team} from "../../../models/team";
import {CdkDragDrop, transferArrayItem} from "@angular/cdk/drag-drop";
import {forkJoin, Observable} from "rxjs";
import {TournamentExtendedProperty} from "../../../models/tournament-extended-property.model";
import {TournamentExtraPropertyKey} from "../../../models/tournament-extra-property-key";
import {TournamentExtendedPropertiesService} from "../../../services/tournament-extended-properties.service";

@Component({
  selector: 'app-senbatsu-fight-creator',
  templateUrl: './senbatsu-fight-dialog-box.component.html',
  styleUrls: ['./senbatsu-fight-dialog-box.component.scss']
})
export class SenbatsuFightDialogBoxComponent extends FightCreator implements OnInit {

  private originalTeams: Team[];
  public teamDragDisabled: Team[];

  constructor(
    protected override teamService: TeamService,
    protected override fightService: FightService,
    protected override groupServices: GroupService,
    protected override messageService: MessageService,
    protected override groupUpdatedService: GroupUpdatedService,
    private tournamentExtendedPropertiesService: TournamentExtendedPropertiesService
  ) {
    super(teamService, fightService, groupServices, messageService, groupUpdatedService);
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
