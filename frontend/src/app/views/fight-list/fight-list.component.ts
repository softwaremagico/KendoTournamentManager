import {Component, OnDestroy, OnInit} from '@angular/core';
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
import {TimeChangedService} from "../../services/notifications/time-changed.service";
import {DuelChangedService} from "../../services/notifications/duel-changed.service";
import {UntieAddedService} from "../../services/notifications/untie-added.service";
import {Group} from "../../models/group";
import {DuelType} from "../../models/duel-type";
import {UserSessionService} from "../../services/user-session.service";
import {MembersOrderChangedService} from "../../services/notifications/members-order-changed.service";
import {takeUntil} from "rxjs";
import {Score} from "../../models/score";
import {RbacBasedComponent} from "../../components/RbacBasedComponent";
import {RbacService} from "../../services/rbac/rbac.service";

@Component({
  selector: 'app-fight-list',
  templateUrl: './fight-list.component.html',
  styleUrls: ['./fight-list.component.scss']
})
export class FightListComponent extends RbacBasedComponent implements OnInit, OnDestroy {

  fights: Fight[];
  unties: Duel[];
  selectedFight: Fight | undefined;
  selectedDuel: Duel | undefined;
  tournament: Tournament;
  timer: boolean = false;
  private readonly tournamentId: number | undefined;
  groups: Group[];
  swappedColors: boolean = false;
  swappedTeams: boolean = false;
  membersOrder: boolean = false;

  constructor(private router: Router, private tournamentService: TournamentService, private fightService: FightService,
              private teamService: TeamService, private groupService: GroupService, private duelService: DuelService,
              public timeChangedService: TimeChangedService, public duelChangedService: DuelChangedService,
              private untieAddedService: UntieAddedService, public dialog: MatDialog, private userSessionService: UserSessionService,
              private membersOrderChangedService: MembersOrderChangedService, private messageService: MessageService,
              public translateService: TranslateService, rbacService: RbacService) {
    super(rbacService);
    let state = this.router.getCurrentNavigation()?.extras.state;
    this.swappedColors = this.userSessionService.getSwappedColors();
    this.swappedTeams = this.userSessionService.getSwappedTeams();
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
    this.refreshFights();
    this.refreshUnties();
    this.untieAddedService.isDuelsAdded.pipe(takeUntil(this.destroySubject)).subscribe(addedDuel => {
      this.refreshUnties();
    });

    this.membersOrderChangedService.membersOrderChanged.pipe(takeUntil(this.destroySubject)).subscribe(_fight => {
      let onlyNewFights: boolean = false;
      let updatedFights: boolean = false;
      if (_fight && this.fights) {
        for (const fight of this.fights) {
          if (onlyNewFights && fight.team1.id === _fight.team1.id) {
            for (let i = 0; i < this.tournament.teamSize; i++) {
              if (!fight.duels[i].duration) {
                fight.duels[i].competitor1 = _fight.duels[i].competitor1;
                this.duelChangedService.isDuelUpdated.next(fight.duels[i]);
                updatedFights = true;
              }
            }
          } else if (onlyNewFights && fight.team2.id === _fight.team2.id) {
            for (let i = 0; i < this.tournament.teamSize; i++) {
              if (!fight.duels[i].duration) {
                fight.duels[i].competitor2 = _fight.duels[i].competitor2;
                this.duelChangedService.isDuelUpdated.next(fight.duels[i]);
                updatedFights = true;
              }
            }
          }
          //Only this fight and the next ones. Not the previous ones.
          if (fight === _fight) {
            onlyNewFights = true;
          }
        }
        if (updatedFights) {
          this.fightService.updateAll(this.fights).subscribe();
        }
      }
    });
  }

  private refreshFights() {
    if (this.tournamentId) {
      this.tournamentService.get(this.tournamentId).subscribe(tournament => {
        this.tournament = tournament;
        if (this.tournamentId) {
          this.groupService.getAllByTournament(this.tournamentId).subscribe(groups => {
            this.groups = groups;
            this.fights = groups.flatMap((group) => group.fights);
            //Use a timeout or refresh before the components are drawn.
            setTimeout(() => {
              if (!this.selectFirstUnfinishedDuel() && this.unties.length === 0) {
                this.showTeamsClassification(true);
              }
            }, 1000);
          });
        }
      });
    }
  }

  private refreshUnties() {
    if (this.tournamentId) {
      this.duelService.getUntiesFromTournament(this.tournamentId).subscribe(duels => {
        this.unties = duels;
        //Use a timeout or refresh before the components are drawn.
        setTimeout(() => {
          if (!this.selectFirstUnfinishedDuel() && this.unties.length > 0) {
            this.showTeamsClassification(true);
          }
        }, 1000);
      });
    }
  }

  private refreshMembersOrder() {

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
        if (result == undefined) {
          //Do nothing
        } else if (result.action === Action.Add) {
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
    if (this.selectedFight || this.selectedDuel) {
      let dialogRef = this.dialog.open(ConfirmationDialogComponent, {
        disableClose: false
      });
      dialogRef.componentInstance.messageTag = "deleteFightWarning"

      dialogRef.afterClosed().subscribe(result => {
        if (result) {
          //Delete the undraw.
          if (this.selectedDuel && this.selectedDuel.type === DuelType.UNDRAW) {
            this.groups[0].unties.splice(this.groups[0].unties.indexOf(this.selectedDuel), 1);
            //Delete the fight.
          } else {
            if (this.selectedFight) {
              this.groups[0].fights.splice(this.groups[0].fights.indexOf(this.selectedFight), 1);
            }
          }
          this.groupService.update(this.groups[0]).subscribe(group => {
            this.messageService.infoMessage("fightDeleted");
            this.refreshFights();
            this.refreshUnties();
          });
        }
      });
    }
  }

  openDialog(title: string, action: Action, fight: Fight) {
    const dialogRef = this.dialog.open(FightDialogBoxComponent, {
      width: '85vw',
      data: {title: 'Add a new Fight', action: Action.Add, entity: new Fight(), tournament: this.tournament}
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result == undefined) {
        //Do nothing
      } else if (result.action === Action.Add) {
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
            this.messageService.infoMessage("infoFightCreated");
          });
        }
      });
    }
  }

  addRowData(fights: Fight[]) {
    this.fightService.addCollection(fights).subscribe(_fights => {
      this.fights.push(..._fights)
      this.messageService.infoMessage("fightStored");
    });
  }

  updateRowData(fight: Fight) {
    this.fightService.update(fight).subscribe(() => {
        this.messageService.infoMessage("infoFightUpdated");
      }
    );
  }

  deleteRowData(fight: Fight) {
    this.fightService.delete(fight).subscribe(() => {
        let currentFights: Fight[] = this.fights.filter(existing_fight => existing_fight !== fight);
        this.messageService.infoMessage("fightDeleted");
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

  showTeamsClassification(fightsFinished: boolean) {
    if (this.groups.length > 0) {
      this.dialog.open(TeamRankingComponent, {
        width: '85vw',
        data: {tournament: this.tournament, groupId: this.groups[0].id, finished: fightsFinished}
      });
    }
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

  setIpponScores(duel: Duel) {
    //Put default points.
    if (duel.competitor1 !== null && duel.competitor2 == null) {
      duel.competitor1Score = [];
      duel.competitor1Score.push(Score.IPPON);
      duel.competitor1Score.push(Score.IPPON);
    } else if (duel.competitor2 !== null && duel.competitor1 == null) {
      duel.competitor2Score = [];
      duel.competitor2Score.push(Score.IPPON);
      duel.competitor2Score.push(Score.IPPON);
    }
  }

  canStartFight(duel: Duel | undefined): boolean {
    return duel !== undefined && duel.competitor1 !== null && duel.competitor2 !== null;
  }

  finishDuel(durationInSeconds: number) {
    if (this.selectedDuel) {
      this.setIpponScores(this.selectedDuel);
      this.selectedDuel.duration = durationInSeconds;
      this.duelService.update(this.selectedDuel).subscribe(duel => {
        this.messageService.infoMessage("infoDuelFinished");
        if (!this.selectFirstUnfinishedDuel()) {
          this.showTeamsClassification(true);
        }
        return duel;
      });
    }
  }

  selectDuel(duel: Duel) {
    this.selectedDuel = duel;
    this.duelChangedService.isDuelUpdated.next(duel);
    if (duel) {
      if (duel.duration) {
        this.timeChangedService.isElapsedTimeChanged.next(duel.duration);
      } else {
        this.timeChangedService.isElapsedTimeChanged.next(0);
      }
    }
    if (duel) {
      if (duel.totalDuration) {
        this.timeChangedService.isTotalTimeChanged.next(duel.totalDuration);
      } else {
        this.timeChangedService.isTotalTimeChanged.next(this.tournament.duelsDuration);
      }
    }
  }

  isOver(duel: Duel): boolean {
    return !!duel.duration;
  }

  areAllDuelsOver(): boolean {
    if (this.fights) {
      for (const fight of this.fights) {
        for (const duel of fight.duels) {
          if (!duel.duration) {
            return false;
          }
        }
      }
      for (const duel of this.unties) {
        if (!duel.duration) {
          return false;
        }
      }
    }
    return true;
  }

  selectFirstUnfinishedDuel(): boolean {
    if (this.fights) {
      for (const fight of this.fights) {
        for (const duel of fight.duels) {
          if (!duel.duration) {
            this.selectedFight = fight;
            this.selectDuel(duel);
            return true;
          }
        }
      }
      for (const duel of this.unties) {
        if (!duel.duration) {
          this.selectedFight = undefined;
          this.selectDuel(duel);
          return true;
        }
      }
    }
    return false;
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

  showTeamTitle(): boolean {
    if (this.tournament?.teamSize) {
      return this.tournament.teamSize > 1;
    }
    return true;
  }

  showSelectedRelatedButton(): boolean {
    return !(this.selectedFight !== undefined || (this.selectedDuel !== undefined && this.selectedDuel.type === DuelType.UNDRAW));
  }

  swapColors() {
    this.swappedColors = !this.swappedColors;
    this.userSessionService.setSwappedColors(this.swappedColors);
  }

  swapTeams() {
    this.swappedTeams = !this.swappedTeams;
    this.userSessionService.setSwappedTeams(this.swappedTeams);
  }

  enableMemberOrder(enabled: boolean) {
    this.membersOrder = enabled;
    this.membersOrderChangedService.membersOrderAllowed.next(enabled);
  }
}
