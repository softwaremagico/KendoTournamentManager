import {Component, OnInit} from '@angular/core';
import {MatDialog} from "@angular/material/dialog";
import {MessageService} from "../../services/message.service";
import {FightService} from "../../services/fight.service";
import {Fight} from "../../models/fight";
import {Tournament} from "../../models/tournament";
import {Router} from "@angular/router";
import {TournamentService} from "../../services/tournament.service";
import {Action} from "../../action";
import {FightDialogBoxComponent} from "./fight-dialog-box/fight-dialog-box.component";
import {TournamentType} from "../../models/tournament-type";
import {LeagueGeneratorComponent} from "./league-generator/league-generator.component";
import {TeamService} from "../../services/team.service";
import {GroupService} from "../../services/group.service";
import {Team} from "../../models/team";
import {ConfirmationDialogComponent} from "../../components/basic/confirmation-dialog/confirmation-dialog.component";
import {TeamRankingComponent} from "./team-ranking/team-ranking.component";
import {CompetitorsRankingComponent} from "./competitors-ranking/competitors-ranking.component";
import {TranslateService} from "@ngx-translate/core";
import {Duel} from "../../models/duel";
import {DuelService} from "../../services/duel.service";
import {DuelChangedService} from "../../services/duel-changed.service";

@Component({
  selector: 'app-fight-list',
  templateUrl: './fight-list.component.html',
  styleUrls: ['./fight-list.component.scss']
})
export class FightListComponent implements OnInit {

  fights: Fight[];
  selectedFight: Fight | undefined;
  selectedDuel: Duel | undefined;
  tournament: Tournament;
  timer: boolean = false;
  private readonly tournamentId: number | undefined;

  constructor(private router: Router, private tournamentService: TournamentService, private fightService: FightService,
              private teamService: TeamService, private groupService: GroupService, private duelService: DuelService,
              public duelChangedService: DuelChangedService, public dialog: MatDialog,
              private messageService: MessageService, public translateService: TranslateService) {
    let state = this.router.getCurrentNavigation()?.extras.state;
    if (state) {
      if (state['tournamentId'] && !isNaN(Number(state['tournamentId']))) {
        this.tournamentId = Number(state['tournamentId']);
      } else {
        this.goBackToTournament();
      }
    } else {
      this.goBackToTournament();
    }
  }

  ngOnInit(): void {
    if (this.tournamentId) {
      this.tournamentService.get(this.tournamentId).subscribe(tournament => {
        this.tournament = tournament;
        this.fightService.getFromTournament(this.tournament).subscribe(fights => {
          this.fights = fights;
          //Use a timeout or refresh before the components are drawn.
          setTimeout(() => {
            this.selectFirstUnfinishedDuel();
          }, 1000);
        });
      });
    }
  }

  openConfirmationGenerateElementsDialog() {
    if (this.fights.length > 0) {
      let dialogRef = this.dialog.open(ConfirmationDialogComponent, {
        disableClose: false
      });
      dialogRef.componentInstance.messageTag = "deleteFightsWarning"

      dialogRef.afterClosed().subscribe(result => {
        if (result) {
          this.generateElements();
        }
      });
    } else {
      this.generateElements();
    }
  }

  generateElements() {
    let dialogRef;
    if (this.tournament.type === TournamentType.LEAGUE) {
      dialogRef = this.dialog.open(LeagueGeneratorComponent, {
        width: '85vw',
        data: {title: 'Create Fights', action: Action.Add, tournament: this.tournament}
      });
    }

    if (dialogRef) {
      dialogRef.afterClosed().subscribe(result => {
        if (result.action === Action.Add) {
          this.createGroupFight(result.data);
        } else if (result.action === Action.Update) {
          this.updateRowData(result.data);
        } else if (result.action === Action.Delete) {
          this.deleteRowData(result.data);
        }
      });
    }
  }

  getDuelDefaultSecondsDuration() {
    if (this.tournament) {
      return this.tournament.duelsDuration % 60;
    }
    return 0;
  }

  getDuelDefaultMinutesDuration() {
    if (this.tournament) {
      return Math.floor(this.tournament.duelsDuration / 60);
    }
    return 0;
  }

  addElement() {
    this.openDialog('Add a new Fight', Action.Add, new Fight());
  }

  editElement(): void {
    if (this.selectedFight) {
      this.openDialog('Edit fight', Action.Update, this.selectedFight);
    }
  }

  deleteElement(): void {
    if (this.selectedFight) {
      this.openDialog('Delete fight', Action.Delete, this.selectedFight);
    }
  }

  openDialog(title: string, action: Action, fight: Fight) {
    const dialogRef = this.dialog.open(FightDialogBoxComponent, {
      width: '85vw',
      data: {title: 'Add a new Fight', action: Action.Add, entity: new Fight(), tournament: this.tournament}
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result.action === Action.Add) {
        //this.createGroupFight();
      } else if (result.action === Action.Update) {
        this.updateRowData(result.data);
      } else if (result.action === Action.Delete) {
        this.deleteRowData(result.data);
      }
    });
  }

  createGroupFight(teams: Team[]) {
    if (this.tournamentId) {
      this.groupService.setTeams(teams).subscribe(() => {
        this.fights = [];
        if (this.tournamentId) {
          this.fightService.create(this.tournamentId, 0, true).subscribe(fights => {
            this.fights = fights;
            this.messageService.infoMessage("Fights Created!");
          });
        }
      });
    }
  }

  addRowData(fights: Fight[]) {
    this.fightService.addCollection(fights).subscribe(_fights => {
      this.fights.push(..._fights)
      this.messageService.infoMessage("Fights Stored");
    });
  }

  updateRowData(fight: Fight) {
    this.fightService.update(fight).subscribe(() => {
        this.messageService.infoMessage("Fight Updated");
      }
    );
  }

  deleteRowData(fight: Fight) {
    this.fightService.delete(fight).subscribe(() => {
        let currentFights: Fight[] = this.fights.filter(existing_fight => existing_fight !== fight);
        this.messageService.infoMessage("Fight Deleted. Current fights: " + currentFights.length);
      }
    );
  }

  goBackToTournament(): void {
    this.router.navigate(['/tournaments'], {});
  }

  selectFight(fight: Fight) {
    this.selectedFight = fight;
  }

  isFightOver(fight: Fight): boolean {
    if (fight) {
      if (!fight.duels) {
        return false;
      }
      for (const duel of fight.duels) {
        if (!duel.duration) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  showTeamsClassification() {
    this.dialog.open(TeamRankingComponent, {
      width: '85vw',
      data: {tournament: this.tournament}
    });
  }

  showCompetitorsClassification() {
    this.dialog.open(CompetitorsRankingComponent, {
      width: '85vw',
      data: {tournament: this.tournament}
    });
  }

  downloadPDF() {
    if (this.tournament && this.tournament.id) {
      this.fightService.getFightSummaryPDf(this.tournament.id).subscribe((pdf: Blob) => {
        const blob = new Blob([pdf], {type: 'application/pdf'});
        const downloadURL = window.URL.createObjectURL(blob);
        let pwa = window.open(downloadURL);
        if (!pwa || pwa.closed || typeof pwa.closed == 'undefined') {
          alert(this.translateService.instant('disablePopUpBlocker'));
        }
      });
    }
  }

  showTimer(show: boolean) {
    this.timer = show;
  }

  finishDuel(durationInSeconds: number) {
    if (this.selectedDuel) {
      this.selectedDuel.duration = durationInSeconds;
      this.duelService.update(this.selectedDuel).subscribe(duel => {
        this.messageService.infoMessage("Duel Finished!");
        this.selectFirstUnfinishedDuel();
        return duel;
      });
    }
  }

  selectDuel(duel: Duel) {
    this.selectedDuel = duel;
    this.duelChangedService.isDuelSelected.next(duel);
  }

  selectFirstUnfinishedDuel() {
    if (this.fights) {
      for (const fight of this.fights) {
        for (const duel of fight.duels) {
          if (!duel.duration) {
            this.selectedFight = fight;
            this.selectDuel(duel);
            return;
          }
        }
      }
    }
  }

  updateDuelDuration(duelDuration: number) {
    if (this.selectedDuel) {
      this.selectedDuel.totalDuration = duelDuration;
      this.duelService.update(this.selectedDuel).subscribe();
    }
  }

  updateDuelElapsedTime(elapsedTime: number) {
    if (this.selectedDuel) {
      this.selectedDuel.duration = elapsedTime;
      this.duelService.update(this.selectedDuel).subscribe();
    }
  }
}
