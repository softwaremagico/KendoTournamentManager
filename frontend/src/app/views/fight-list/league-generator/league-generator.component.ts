import {Component, Inject, OnInit, Optional} from '@angular/core';
import {Action} from "../../../action";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {Tournament} from "../../../models/tournament";
import {TeamListData} from "../../../components/basic/team-list/team-list-data";
import {TeamService} from "../../../services/team.service";
import {CdkDragDrop, transferArrayItem} from "@angular/cdk/drag-drop";
import {Team} from "../../../models/team";
import {RbacBasedComponent} from "../../../components/RbacBasedComponent";
import {RbacService} from "../../../services/rbac/rbac.service";
import {TournamentType} from "../../../models/tournament-type";
import {TournamentService} from "../../../services/tournament.service";
import {UntypedFormControl} from "@angular/forms";
import {TournamentExtendedPropertiesService} from "../../../services/tournament-extended-properties.service";
import {TournamentExtraPropertyKey} from "../../../models/tournament-extra-property-key";
import {TournamentExtendedProperty} from "../../../models/tournament-extended-property.model";
import {MessageService} from "../../../services/message.service";
import {DrawResolution} from "../../../models/draw-resolution";
import {MatSlideToggleChange} from "@angular/material/slide-toggle";
import {LeagueFightsOrder} from "../../../models/league-fights-order";
import {Participant} from "../../../models/participant";
import {ScoreOfCompetitor} from "../../../models/score-of-competitor";
import {RankingService} from "../../../services/ranking.service";

@Component({
  selector: 'app-league-generator',
  templateUrl: './league-generator.component.html',
  styleUrls: ['./league-generator.component.scss']
})
export class LeagueGeneratorComponent extends RbacBasedComponent implements OnInit {

  teamListData: TeamListData = new TeamListData();
  teams: Team[];
  title: string;
  action: Action;
  actionName: string;
  teamsOrder: Team[] = [];
  tournament: Tournament;
  drawResolution: DrawResolution[];
  avoidDuplicates = new UntypedFormControl('', []);

  //Enable
  canMaximizeFights: boolean;
  needsDrawResolution: boolean;
  needsFifoWinner: boolean;
  canAvoidDuplicatedFights: boolean;

  //Values
  areFightsMaximized: boolean;
  firstInFirstOut: boolean;
  selectedDrawResolution: DrawResolution;
  avoidDuplicatedFights: boolean;

  constructor(public dialogRef: MatDialogRef<LeagueGeneratorComponent>,
              private teamService: TeamService, rbacService: RbacService, private tournamentService: TournamentService,
              private tournamentExtendedPropertiesService: TournamentExtendedPropertiesService,
              private messageService: MessageService, private rankingService: RankingService,
              @Optional() @Inject(MAT_DIALOG_DATA) public data: {
                title: string,
                action: Action,
                tournament: Tournament
              }) {
    super(rbacService);
    this.title = data.title;
    this.action = data.action;
    this.actionName = Action[data.action];
    this.tournament = data.tournament;
    this.drawResolution = DrawResolution.toArray();

    this.canMaximizeFights = TournamentType.canMaximizeFights(this.tournament.type);
    this.needsDrawResolution = TournamentType.needsDrawResolution(this.tournament.type);
    this.needsFifoWinner = TournamentType.needsFifoWinner(this.tournament.type);
    this.canAvoidDuplicatedFights = TournamentType.avoidsDuplicatedFights(this.tournament.type);

    this.defaultPropertiesValue();
  }

  ngOnInit(): void {
    if (this.canMaximizeFights || this.needsDrawResolution || this.needsFifoWinner) {
      this.tournamentExtendedPropertiesService.getByTournament(this.tournament).subscribe((_tournamentSelection: TournamentExtendedProperty[]): void => {
        if (_tournamentSelection) {
          for (const _tournamentProperty of _tournamentSelection) {
            if (_tournamentProperty.propertyKey == TournamentExtraPropertyKey.KING_DRAW_RESOLUTION) {
              this.selectedDrawResolution = DrawResolution.getByKey(_tournamentProperty.propertyValue);
            }
            if (_tournamentProperty.propertyKey == TournamentExtraPropertyKey.MAXIMIZE_FIGHTS) {
              this.areFightsMaximized = (_tournamentProperty.propertyValue.toLowerCase() == "true");
            }
            if (_tournamentProperty.propertyKey == TournamentExtraPropertyKey.LEAGUE_FIGHTS_ORDER_GENERATION) {
              this.firstInFirstOut = (_tournamentProperty.propertyValue.toUpperCase() == LeagueFightsOrder.FIFO);
            }
          }
        }
      });
    }

    this.teamService.getFromTournament(this.tournament).subscribe((_teams: Team[]): void => {
      if (_teams) {
        _teams.sort(function (a: Team, b: Team) {
          return a.name.localeCompare(b.name);
        });
      }
      this.teams = _teams;
      this.teamListData.teams = _teams;
      this.teamListData.filteredTeams = _teams;
    });
    this.avoidDuplicates.valueChanges.subscribe(avoidDuplicates => {
      const tournamentProperty: TournamentExtendedProperty = new TournamentExtendedProperty();
      tournamentProperty.tournament = this.tournament;
      tournamentProperty.propertyValue = !avoidDuplicates + "";
      tournamentProperty.propertyKey = TournamentExtraPropertyKey.MAXIMIZE_FIGHTS;
      this.tournamentExtendedPropertiesService.update(tournamentProperty).subscribe((): void => {
        this.messageService.infoMessage('infoTournamentUpdated');
      });
    });
  }

  defaultPropertiesValue(): void {
    this.areFightsMaximized = TournamentExtraPropertyKey.getDefaultMaximizedFights();
    this.selectedDrawResolution = TournamentExtraPropertyKey.getDefaultKingDrawResolutions();
    this.firstInFirstOut = TournamentExtraPropertyKey.getDefaultLeagueFightsOrderGeneration();
    this.avoidDuplicatedFights = TournamentExtraPropertyKey.avoidDuplicateFightsGeneration();
  }

  acceptAction() {
    this.dialogRef.close({data: this.teamsOrder, action: this.action});
  }

  cancelDialog() {
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

  dropTeam(event: CdkDragDrop<Team[], any>): void {
    const team: Team = this.transferCard(event);
    if (this.teamListData.teams.includes(team)) {
      this.teamListData.teams.splice(this.teamListData.teams.indexOf(team), 1);
    }
    if (this.teamListData.filteredTeams.includes(team)) {
      this.teamListData.filteredTeams.splice(this.teamListData.filteredTeams.indexOf(team), 1);
    }
  }

  sortedTeams(): void {
    this.teamsOrder.push(...this.teamListData.teams);
    this.teamsOrder.sort(function (a: Team, b: Team) {
      return a.name.localeCompare(b.name);
    });
    this.teamListData.filteredTeams.splice(0, this.teamListData.filteredTeams.length);
    this.teamListData.teams.splice(0, this.teamListData.teams.length);
  }

  randomTeams(): void {
    this.teamListData.teams.push(...this.teamsOrder);
    this.teamsOrder = [];
    while (this.teamListData.teams.length > 0) {
      const team: Team = this.getRandomTeam(this.teamListData.teams);
      this.teamsOrder.push(team);
    }
  }

  getRandomTeam(teams: Team[]): Team {
    const selected: number = Math.floor(Math.random() * teams.length);
    const team: Team = teams[selected];
    teams.splice(selected, 1);
    return team;
  }

  balancedTeams(): void {
    let participants: (Participant | undefined)[] = this.teams.flatMap((team: Team) => team.members);

    this.rankingService.getCompetitorsGlobalScoreRanking(participants, undefined).subscribe((_scoreRanking: ScoreOfCompetitor[]): void => {
      const sortedParticipants: Participant[] = _scoreRanking.map((scoreOfCompetitor: ScoreOfCompetitor) => scoreOfCompetitor.competitor);
      //Get Teams classification by members index.
      const teamsScore: Map<number, Team> = new Map();
      for (let team of this.teams) {
        //Accumulate the index of each member. Lower member means better participant statistics.
        let score: number = 0;
        for (let member of team.members) {
          if (member != undefined) {
            score += sortedParticipants.indexOf(<Participant>sortedParticipants.find(p => p.id === member?.id));
          }
        }
        //Ensure not collides.
        while (teamsScore.get(score)) {
          score++;
        }
        teamsScore.set(score, team);
      }

      //Sort map and assign:
      this.teamsOrder = [];
      const sortedTeams: Map<number, Team> = new Map([...teamsScore.entries()].sort(
        (a: [number, Team], b: [number, Team]) => a[0] - b[0]));
      this.teamsOrder.push(...sortedTeams.values());
      this.teamListData.teams = [];
      this.teamListData.filteredTeams = [];
      if (this.tournament.type == TournamentType.LOOP) {
        this.teamsOrder = this.teamsOrder.reverse();
      }
    });
  }


  getDrawResolutionTranslationTag(drawResolution: DrawResolution): string {
    if (!drawResolution) {
      return "";
    }
    return DrawResolution.toCamel(drawResolution);
  }

  getDrawResolutionHintTag(drawResolution: DrawResolution): string {
    if (!drawResolution) {
      return "";
    }
    return DrawResolution.toCamel(drawResolution) + "Hint";
  }

  maxFightsToggle($event: MatSlideToggleChange): void {
    const tournamentProperty: TournamentExtendedProperty = new TournamentExtendedProperty();
    tournamentProperty.tournament = this.tournament;
    tournamentProperty.propertyValue = $event.checked + "";
    tournamentProperty.propertyKey = TournamentExtraPropertyKey.MAXIMIZE_FIGHTS;
    this.tournamentExtendedPropertiesService.update(tournamentProperty).subscribe((): void => {
      this.messageService.infoMessage('infoTournamentUpdated');
    });
  }

  selectDrawResolution(drawResolution: DrawResolution): void {
    this.selectedDrawResolution = drawResolution;
    const tournamentProperty: TournamentExtendedProperty = new TournamentExtendedProperty();
    tournamentProperty.tournament = this.tournament;
    tournamentProperty.propertyValue = drawResolution;
    tournamentProperty.propertyKey = TournamentExtraPropertyKey.KING_DRAW_RESOLUTION;
    this.tournamentExtendedPropertiesService.update(tournamentProperty).subscribe((): void => {
      this.messageService.infoMessage('infoTournamentUpdated');
    });
  }

  fifoToggle($event: MatSlideToggleChange): void {
    const tournamentProperty: TournamentExtendedProperty = new TournamentExtendedProperty();
    tournamentProperty.tournament = this.tournament;
    tournamentProperty.propertyValue = $event.checked ? LeagueFightsOrder.FIFO : LeagueFightsOrder.LIFO;
    tournamentProperty.propertyKey = TournamentExtraPropertyKey.LEAGUE_FIGHTS_ORDER_GENERATION;
    this.tournamentExtendedPropertiesService.update(tournamentProperty).subscribe((): void => {
      this.messageService.infoMessage('infoTournamentUpdated');
    });
  }

  avoidDuplicatesToggle($event: MatSlideToggleChange): void {
    const tournamentProperty: TournamentExtendedProperty = new TournamentExtendedProperty();
    tournamentProperty.tournament = this.tournament;
    tournamentProperty.propertyValue = $event.checked + "";
    tournamentProperty.propertyKey = TournamentExtraPropertyKey.AVOID_DUPLICATES;
    this.tournamentExtendedPropertiesService.update(tournamentProperty).subscribe((): void => {
      this.messageService.infoMessage('infoTournamentUpdated');
    });
  }

  protected readonly TournamentType = TournamentType;
}
