import {Component, OnDestroy, OnInit} from '@angular/core';
import {MatDialog, MatDialogRef} from "@angular/material/dialog";
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
import {GroupService} from "../../services/group.service";
import {Team} from "../../models/team";
import {ConfirmationDialogComponent} from "../../components/basic/confirmation-dialog/confirmation-dialog.component";
import {TeamRankingComponent} from "../../components/team-ranking/team-ranking.component";
import {CompetitorsRankingComponent} from "../../components/competitors-ranking/competitors-ranking.component";
import {Duel} from "../../models/duel";
import {DuelService} from "../../services/duel.service";
import {TimeChangedService} from "../../services/notifications/time-changed.service";
import {DuelChangedService} from "../../services/notifications/duel-changed.service";
import {UntieAddedService} from "../../services/notifications/untie-added.service";
import {Group} from "../../models/group";
import {DuelType} from "../../models/duel-type";
import {UserSessionService} from "../../services/user-session.service";
import {MembersOrderChangedService} from "../../services/notifications/members-order-changed.service";
import {Subject, Subscription, takeUntil} from "rxjs";
import {Score} from "../../models/score";
import {RbacBasedComponent} from "../../components/RbacBasedComponent";
import {RbacService} from "../../services/rbac/rbac.service";
import {GroupUpdatedService} from "../../services/notifications/group-updated.service";
import {SystemOverloadService} from "../../services/notifications/system-overload.service";
import {TranslateService} from "@ngx-translate/core";
import {RxStompService} from "../../websockets/rx-stomp.service";
import {Message} from "@stomp/stompjs";

@Component({
  selector: 'app-fight-list',
  templateUrl: './fight-list.component.html',
  styleUrls: ['./fight-list.component.scss']
})
export class FightListComponent extends RbacBasedComponent implements OnInit, OnDestroy {

  filteredFights: Map<number, Fight[]>;
  filteredUnties: Map<number, Duel[]>;
  //Check if a level label must be shown or not.
  filteredLevels: number[];

  selectedFight: Fight | undefined;
  selectedDuel: Duel | undefined;
  selectedGroup: Group | undefined;

  tournament: Tournament;
  timer: boolean = false;
  private readonly tournamentId: number | undefined;
  groups: Group[];
  swappedColors: boolean = false;
  swappedTeams: boolean = false;
  membersOrder: boolean = false;
  isWizardEnabled: boolean;
  isBracketsEnabled: boolean;
  kingOfTheMountainType: TournamentType = TournamentType.KING_OF_THE_MOUNTAIN;
  showAvatars: boolean = false;

  resetFilterValue: Subject<boolean> = new Subject();

  resetTimerPosition: Subject<boolean> = new Subject();

  showLevelTags: boolean = false;
  showLevelOfGroup: Map<Group, boolean> = new Map<Group, boolean>;

  selectedShiaijo: number = -1;

  private topicSubscription: Subscription;


  constructor(private router: Router, private tournamentService: TournamentService, private fightService: FightService,
              private groupService: GroupService, private duelService: DuelService,
              private timeChangedService: TimeChangedService, private duelChangedService: DuelChangedService,
              private untieAddedService: UntieAddedService, private groupUpdatedService: GroupUpdatedService,
              private dialog: MatDialog, private userSessionService: UserSessionService,
              private membersOrderChangedService: MembersOrderChangedService, private messageService: MessageService,
              rbacService: RbacService, private translateService: TranslateService,
              private systemOverloadService: SystemOverloadService,
              private rxStompService: RxStompService) {
    super(rbacService);
    this.filteredFights = new Map<number, Fight[]>();
    this.filteredUnties = new Map<number, Duel[]>();
    this.filteredLevels = [];
    this.groups = [];
    const state = this.router.getCurrentNavigation()?.extras.state;
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
    this.swappedColors = this.userSessionService.getSwappedColors();
    this.swappedTeams = this.userSessionService.getSwappedTeams();
    this.systemOverloadService.isTransactionalBusy.next(true);
    if (this.tournamentId) {
      this.tournamentService.get(this.tournamentId).subscribe((tournament: Tournament): void => {
        this.tournament = tournament;
        this.refreshFights();
      });
    }
    this.untieAddedService.isDuelsAdded.pipe(takeUntil(this.destroySubject)).subscribe((): void => {
      this.refreshFights();
    });
    this.groupUpdatedService.isGroupUpdated.pipe(takeUntil(this.destroySubject)).subscribe((_group: Group): void => {
      this.replaceGroup(_group);
    })

    this.membersOrderChangedService.membersOrderChanged.pipe(takeUntil(this.destroySubject)).subscribe((_fight: Fight): void => {
      let onlyNewFights: boolean = false;
      let updatedFights: boolean = false;
      if (_fight && this.groups) {
        this.resetFilter();
        for (const group of this.groups) {
          for (const fight of group.fights) {
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
        }
        if (updatedFights) {
          this.fightService.updateAll(this.getFights()).subscribe();
        }
      }
      this.systemOverloadService.isTransactionalBusy.next(false);
    });

    this.topicSubscription = this.rxStompService.watch('/frontend/fights').subscribe((message: Message): void => {
      console.log(message.body);
    });
  }

  override ngOnDestroy(): void {
    super.ngOnDestroy();
    this.topicSubscription.unsubscribe();
  }

  private replaceGroup(group: Group): void {
    if (group && this.groups) {
      let selectedFightIndex: number | undefined;
      let selectedDuelIndex: number | undefined

      //Corrected selected items
      if (this.selectedFight) {
        for (const _group of this.groups) {
          if (_group.fights.indexOf(this.selectedFight)) {
            selectedFightIndex = _group.fights.indexOf(this.selectedFight);
          }
        }
        selectedDuelIndex = this.selectedFight?.duels.indexOf(this.selectedDuel!);
      }

      const groupIndex: number = this.groups.map((group: Group) => group.id).indexOf(group.id);
      this.groups.splice(groupIndex, 1, group);
      this.selectedGroup = this.groups[groupIndex];
      this.resetFilter();
      if (this.selectedGroup && this.selectedFight && selectedFightIndex) {
        this.selectFight(this.filteredFights.get(this.selectedGroup.id!)![selectedFightIndex]);
      } else {
        this.selectFight(undefined);
      }
      if (this.selectedFight && selectedDuelIndex && this.selectedFight?.duels[selectedDuelIndex]) {
        this.selectDuel(this.selectedFight.duels[selectedDuelIndex]);
      }
    }
  }

  private getFights(): Fight[] {
    return this.groups.flatMap((group: Group) => group.fights);
  }

  private getUnties(): Duel[] {
    return this.groups.flatMap((group: Group) => group.unties)
  }

  private refreshFights(): void {
    if (this.tournament) {
      this.isWizardEnabled = this.tournament.type !== TournamentType.CUSTOMIZED && this.tournament.type !== TournamentType.CHAMPIONSHIP;
      this.isBracketsEnabled = this.tournament.type === TournamentType.CHAMPIONSHIP;
      if (this.tournamentId) {
        this.groupService.getFromTournament(this.tournamentId).subscribe((_groups: Group[]): void => {
          if (!_groups) {
            this.messageService.errorMessage('No groups on tournament!');
          } else {
            this.setGroups(_groups);
          }
        });
      }
    }
  }

  private setGroups(groups: Group[]): void {
    groups.sort((a: Group, b: Group): number => {
      if (a.level === b.level) {
        return a.index - b.index;
      }
      return a.level - b.level;
    });
    const fights: Fight[] = groups.flatMap((group: Group) => group.fights);
    for (let fight of fights) {
      for (let duel of fight.duels) {
        if (duel.competitor1?.hasAvatar || duel.competitor2?.hasAvatar) {
          this.showAvatars = true;
        }
      }
    }
    this.groups = groups;

    //Set level tags
    this.setLevelTagVisibility(groups);

    if (groups.length > 0) {
      this.selectedGroup = groups[0];
    }

    this.resetFilter();
    //Use a timeout or refresh before the components are drawn.
    setTimeout(() => {
      if (!this.selectFirstUnfinishedDuel() && this.getUnties().length === 0) {
        this.showTeamsClassification(true);
      }
    }, 1000);
  }

  private setLevelTagVisibility(sortedGroups: Group[]): void {
    const showedLevel: boolean [] = []
    this.showLevelOfGroup = new Map<Group, boolean>();
    for (let group of sortedGroups) {
      if (group.level >= showedLevel.length) {
        showedLevel.push(true);
        //Hide level label if it hasn't fights on any of its groups.
        const groupsOfLevelWithFights: Group[] = sortedGroups.filter(group => group.level == showedLevel.length - 1 && group.fights.length > 0);
        if (groupsOfLevelWithFights.length == 0) {
          showedLevel[showedLevel.length - 1] = false;
        }
      }
      this.showLevelOfGroup.set(group, showedLevel[group.level]);
      showedLevel[group.level] = false;
    }

    this.showLevelTags = showedLevel.length > 1;
  }

  openConfirmationGenerateElementsDialog(): void {
    if (this.getFights().length > 0) {
      let dialogRef: MatDialogRef<ConfirmationDialogComponent> = this.dialog.open(ConfirmationDialogComponent, {
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

  openBracketsManager(): void {
    if (this.tournament.type === TournamentType.CHAMPIONSHIP) {
      this.router.navigate(['tournaments/fights/championship'], {
        state: {
          tournamentId: this.tournament.id,
          editionDisabled: this.getFights().length > 0
        }
      });
    }
  }

  generateElements(): void {
    let dialogRef;
    if (this.tournament.type === TournamentType.LEAGUE || this.tournament.type === TournamentType.LOOP ||
      this.tournament.type === TournamentType.KING_OF_THE_MOUNTAIN) {
      dialogRef = this.dialog.open(LeagueGeneratorComponent, {
        width: '85vw',
        data: {title: 'Create Fights', action: Action.Add, tournament: this.tournament}
      });
    } else if (this.tournament.type === TournamentType.CHAMPIONSHIP) {
      this.openBracketsManager();
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

  getDuelDefaultSecondsDuration(): number {
    if (this.tournament) {
      return this.tournament.duelsDuration % 60;
    }
    return 0;
  }

  getDuelDefaultMinutesDuration(): number {
    if (this.tournament) {
      return Math.floor(this.tournament.duelsDuration / 60);
    }
    return 0;
  }

  addElement(): void {
    //Ensure that is selected on the typical case.
    if (this.groups.length == 1) {
      this.selectedGroup = this.groups[0];
    }
    if (this.selectedGroup) {
      const fight: Fight = new Fight();
      fight.tournament = this.tournament;
      fight.shiaijo = 0;
      fight.level = this.selectedGroup.level;
      fight.duels = [];
      this.openAddFightDialog('Add a new Fight', Action.Add, fight, this.selectedGroup, this.selectedFight);
    } else {
      this.messageService.warningMessage('errorFightNotSelected');
    }
  }

  editElement(): void {
    if (this.selectedFight && this.selectedGroup) {
      this.openAddFightDialog('Edit fight', Action.Update, this.selectedFight, this.selectedGroup, undefined);
    }
  }

  deleteElement(): void {
    if (this.selectedFight || this.selectedDuel) {
      let dialogRef = this.dialog.open(ConfirmationDialogComponent, {
        disableClose: false
      });
      dialogRef.componentInstance.messageTag = "deleteFightWarning"
      dialogRef.componentInstance.parameters = {
        team1: !this.swappedTeams ? (this.selectedFight?.team1.name) : (this.selectedFight?.team2.name),
        team2: !this.swappedTeams ? (this.selectedFight?.team2.name) : (this.selectedFight?.team1.name),
      }

      dialogRef.afterClosed().subscribe(result => {
        if (result) {
          //Delete undraw.
          if (this.selectedDuel && this.selectedGroup && this.selectedDuel.type === DuelType.UNDRAW) {
            this.selectedGroup.unties.splice(this.selectedGroup.unties.indexOf(this.selectedDuel), 1);
            //Delete the fight.
          } else {
            if (this.selectedFight && this.selectedGroup) {
              this.selectedGroup.fights.splice(this.selectedGroup.fights.indexOf(this.selectedFight), 1);
            }
          }
          if (this.selectedGroup) {
            this.groupService.update(this.selectedGroup).subscribe(() => {
              this.messageService.infoMessage("fightDeleted");
              this.refreshFights();
            });
          }
        }
      });
    }
  }

  openAddFightDialog(title: string, action: Action, fight: Fight, group: Group, afterFight: Fight | undefined): void {
    const dialogRef = this.dialog.open(FightDialogBoxComponent, {
      width: '90vw',
      height: '95vh',
      maxWidth: '1000px',
      data: {
        title: 'Add a new Fight',
        action: Action.Add,
        entity: fight,
        group: group,
        previousFight: afterFight,
        tournament: this.tournament,
        swappedColors: this.swappedColors,
        swappedTeams: this.swappedTeams
      }
    });
    dialogRef.afterClosed().subscribe(result => {
      if (result == undefined) {
        //Do nothing
      } else if (result.action === Action.Add) {
        this.selectFirstUnfinishedDuel();
      } else if (result.action === Action.Update) {
        this.updateRowData(result.data);
      } else if (result.action === Action.Delete) {
        this.deleteRowData(result.data);
      }
    });
  }

  createGroupFight(teams: Team[]): void {
    if (this.tournamentId) {
      this.groupService.setTeams(teams).subscribe((_group: Group): void => {
        this.selectedGroup = _group;
        if (this.tournamentId) {
          this.fightService.create(this.tournamentId, 0).subscribe((fights: Fight[]): void => {
            this.resetFilter();
            this.selectedGroup!.fights = fights;
            this.messageService.infoMessage("infoFightCreated");
            this.refreshFights();
          });
        }
      });
    }
  }

  updateRowData(fight: Fight): void {
    this.fightService.update(fight).subscribe((): void => {
        this.messageService.infoMessage("infoFightUpdated");
      }
    );
  }

  deleteRowData(fight: Fight): void {
    this.fightService.delete(fight).subscribe(() => {
        this.selectedGroup!.fights = this.selectedGroup!.fights.filter((existing_fight: Fight): boolean => existing_fight !== fight);
        this.filteredFights.set(this.selectedGroup!.id!, this.filteredFights.get(this.selectedGroup!.id!)!.filter(
          (existing_fight: Fight): boolean => existing_fight !== fight));
        this.messageService.infoMessage("fightDeleted");
      }
    );
  }

  goBackToTournament(): void {
    this.router.navigate(['/tournaments'], {});
  }

  selectFight(fight: Fight | undefined): void {
    this.selectedFight = fight;
    if (fight) {
      this.selectedGroup = this.groups.find((group: Group): boolean => group.fights.indexOf(fight) >= 0)!;
    } else {
      this.selectedGroup = undefined;
    }
  }

  isFightOver(fight: Fight): boolean {
    if (fight) {
      if (!fight.duels) {
        return false;
      }
      for (const duel of fight.duels) {
        if (!duel.finished) {
          return false;
        }
      }
      return true;
    }
    return false;
  }

  showTeamsClassification(fightsFinished: boolean): void {
    if (this.groups.length > 0 && this.getFights().length > 0) {
      const dialogRef: MatDialogRef<TeamRankingComponent> = this.dialog.open(TeamRankingComponent, {
        width: '85vw',
        data: {tournament: this.tournament, group: this.selectedGroup, finished: fightsFinished}
      });
      dialogRef.afterClosed().subscribe(result => {
        if (result.action === Action.Cancel) {
        }
      });
    }
  }

  showCompetitorsClassification(): void {
    this.dialog.open(CompetitorsRankingComponent, {
      width: '85vw',
      data: {tournament: this.tournament}
    });
  }

  downloadPDF() {
    if (this.tournament && this.tournament.id) {
      this.fightService.getFightSummaryPDf(this.tournament.id).subscribe((pdf: Blob): void => {
        const blob = new Blob([pdf], {type: 'application/pdf'});
        const downloadURL = window.URL.createObjectURL(blob);

        const anchor = document.createElement("a");
        anchor.download = "Fight List - " + this.tournament.name + ".pdf";
        anchor.href = downloadURL;
        anchor.click();
      });
    }
  }

  showTimer(show: boolean): void {
    this.timer = show;
    this.resetTimerPosition.next(show);
  }

  setIpponScores(duel: Duel): void {
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

  removeIpponScores(duel: Duel): void {
    for (let i: number = 0; i < duel.competitor1Score.length; i++) {
      if (duel.competitor1Score[i] == Score.IPPON) {
        duel.competitor1Score = [];
      }
    }
    for (let i: number = 0; i < duel.competitor2Score.length; i++) {
      if (duel.competitor2Score[i] == Score.IPPON) {
        duel.competitor2Score = [];
      }
    }
  }

  canStartFight(duel: Duel | undefined): boolean {
    return duel?.competitor1 !== null && duel?.competitor2 !== null;
  }

  finishDuel(): void {
    if (this.selectedDuel) {
      this.setIpponScores(this.selectedDuel);
      this.selectedDuel.finished = true;
      if (!this.selectedDuel.finishedAt) {
        this.selectedDuel.finishedAt = new Date();
      }
      this.duelService.update(this.selectedDuel).subscribe((): void => {
        this.messageService.infoMessage("infoDuelFinished");
        const selectedGroup: Group | null = this.getGroup(this.selectedDuel);
        let showClassification: boolean = true;
        if (selectedGroup != null) {
          // Tournament, each group must have a winner. Show for each group the winners.
          if (Group.isFinished(selectedGroup) && this.tournament.type !== TournamentType.KING_OF_THE_MOUNTAIN) {
            //Shows group classification. And if there is a tie score can be solved.
            this.showClassification();
            showClassification = false;
          }
        }
        // King of the mountain. Generate infinite fights.
        if (!this.selectFirstUnfinishedDuel()) {
          this.generateNextFights(showClassification && this.tournament.type !== TournamentType.KING_OF_THE_MOUNTAIN);
        }
      });
    }
  }

  unfinishDuel(): void {
    if (this.selectedDuel) {
      this.removeIpponScores(this.selectedDuel);
      this.selectedDuel.finished = false;
      this.selectedDuel.finishedAt = undefined;
      this.duelService.update(this.selectedDuel).subscribe((): void => {
      });
    }
  }

  getGroup(selectedDuel: Duel | undefined): Group | null {
    if (!selectedDuel) {
      return null;
    }
    for (const group of this.groups) {
      for (const fight of group.fights) {
        for (const duel of fight.duels) {
          if (duel.id == selectedDuel.id) {
            return group;
          }
        }
        for (const duel of group.unties) {
          if (duel.id == selectedDuel.id) {
            return group;
          }
        }
      }
    }
    return null;
  }

  generateNextFights(showClassification: boolean): void {
    const selectedGroup: Group | null = this.getGroup(this.selectedDuel);
    this.fightService.createNext(this.tournamentId!).subscribe((_fights: Fight[]): void => {
      //Null value means that fights are not created due to an existing draw score.
      if (_fights === null) {
        //Do nothing. A Draw fight that must be solved.
      } else if (_fights.length > 0) {
        this.refreshFights();
      } else {
        if (showClassification) {
          this.showClassification();
        }
        this.finishTournament(new Date());
      }
    });
  }

  showClassification(): void {
    if ((this.tournament?.teamSize && this.tournament?.teamSize > 1) ||
      (this.tournament && (this.tournament.type === TournamentType.KING_OF_THE_MOUNTAIN || this.tournament.type === TournamentType.CHAMPIONSHIP))) {
      this.showTeamsClassification(true);
    } else {
      this.showCompetitorsClassification();
    }
  }

  finishTournament(date: Date | undefined): void {
    if (!this.tournament.finishedAt && date) {
      this.tournament.finishedAt = date;
      this.tournamentService.update(this.tournament).subscribe();
    } else if (!date && this.tournament.finishedAt) {
      this.tournament.finishedAt = undefined
      this.tournamentService.update(this.tournament).subscribe();
    }
  }

  selectDuel(duel: Duel): void {
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
    return duel.finished;
  }

  areAllDuelsOver(): boolean {
    const fights: Fight[] = this.getFights();
    const unties: Duel[] = this.getUnties();
    if (fights) {
      for (const fight of fights) {
        for (const duel of fight.duels) {
          if (!duel.finished) {
            return false;
          }
        }
      }
      for (const duel of unties) {
        if (!duel.finished) {
          return false;
        }
      }
    }
    return true;
  }

  selectFirstUnfinishedDuel(): boolean {
    this.resetFilter();
    const fights: Fight[] = this.getFights();
    const unties: Duel[] = this.getUnties();
    if (fights) {
      for (const fight of fights) {
        for (const duel of fight.duels) {
          if (!duel.finished) {
            this.selectedFight = fight;
            this.selectDuel(duel);
            return true;
          }
        }
      }
      for (const duel of unties) {
        if (!duel.finished) {
          this.selectedFight = undefined;
          this.selectDuel(duel);
          return true;
        }
      }
    }
    return false;
  }

  updateDuelDuration(duelDuration: number): void {
    if (this.selectedDuel) {
      this.selectedDuel.totalDuration = duelDuration;
      this.duelService.update(this.selectedDuel).subscribe();
    }
  }

  updateDuelElapsedTime(elapsedTime: number, updateBackend: boolean): void {
    if (this.selectedDuel) {
      this.selectedDuel.duration = elapsedTime;
      if (updateBackend) {
        this.duelService.update(this.selectedDuel).subscribe();
      }
    }
  }

  duelStarted(elapsedTime: number): void {
    if (this.selectedDuel && !this.selectedDuel.duration && !this.selectedDuel.startedAt) {
      this.selectedDuel.duration = elapsedTime;
      this.selectedDuel.startedAt = new Date();
      this.duelService.update(this.selectedDuel).subscribe();
    }
  }

  showSelectedRelatedButton(): boolean {
    return !(this.selectedFight !== undefined || (this.selectedDuel !== undefined && this.selectedDuel.type === DuelType.UNDRAW));
  }

  swapColors(): void {
    this.swappedColors = !this.swappedColors;
    this.userSessionService.setSwappedColors(this.swappedColors);
  }

  swapTeams(): void {
    this.swappedTeams = !this.swappedTeams;
    this.userSessionService.setSwappedTeams(this.swappedTeams);
  }

  enableMemberOrder(enabled: boolean): void {
    this.membersOrder = enabled;
    this.membersOrderChangedService.membersOrderAllowed.next(enabled);
  }

  filter(filter: string): void {
    filter = filter.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "");
    this.filteredFights = new Map<number, Fight[]>();
    this.filteredUnties = new Map<number, Duel[]>();
    this.filteredLevels = [];

    for (const group of this.groups) {
      if (group.fights) {
        this.filteredFights.set(group.id!, group.fights.filter((fight: Fight): boolean =>
          (this.selectedShiaijo < 0 || fight.shiaijo == this.selectedShiaijo) && (
            fight.team1.name.normalize('NFD').replace(/\p{Diacritic}/gu, "").toLowerCase().includes(filter) ||
            fight.team2.name.normalize('NFD').replace(/\p{Diacritic}/gu, "").toLowerCase().includes(filter) ||
            fight.team1.members.some(user => user !== undefined && (user.lastname.normalize('NFD').replace(/\p{Diacritic}/gu, "").toLowerCase().includes(filter) ||
              user.name.normalize('NFD').replace(/\p{Diacritic}/gu, "").toLowerCase().includes(filter) ||
              (user.club ? user.club.name.normalize('NFD').replace(/\p{Diacritic}/gu, "").toLowerCase().includes(filter) : ""))) ||
            fight.team2.members.some(user => user !== undefined && (user.lastname.normalize('NFD').replace(/\p{Diacritic}/gu, "").toLowerCase().includes(filter) ||
              user.name.normalize('NFD').replace(/\p{Diacritic}/gu, "").toLowerCase().includes(filter) ||
              (user.club ? user.club.name.normalize('NFD').replace(/\p{Diacritic}/gu, "").toLowerCase().includes(filter) : ""))))));
      } else {
        this.filteredFights.set(group.id!, []);
      }

      if (group.unties) {
        this.filteredUnties.set(group.id!, group.unties.filter((duel: Duel) =>
          (this.selectedShiaijo < 0 || group.shiaijo == this.selectedShiaijo) && (
            duel.competitor1!.lastname.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "").includes(filter) ||
            duel.competitor1!.name.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "").includes(filter) || duel.competitor1!.idCard.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "").includes(filter) ||
            (duel.competitor1!.club ? duel.competitor1!.club.name.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "").includes(filter) : "") ||

            duel.competitor2!.lastname.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "").includes(filter) ||
            duel.competitor2!.name.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "").includes(filter) || duel.competitor2!.idCard.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "").includes(filter) ||
            (duel.competitor2!.club ? duel.competitor2!.club.name.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "").includes(filter) : ""))));
      } else {
        this.filteredUnties.set(group.id!, []);
      }

      //Check if a level label must be shown or not.
      for (let fights of this.filteredFights.values()) {
        for (let fight of fights) {
          if (!this.filteredLevels.includes(fight.level)) {
            this.filteredLevels.push(fight.level);
          }
        }
      }
    }
  }

  resetFilter(): void {
    this.filter('');
    this.resetFilterValue.next(true);
  }

  getShiaijoTag(): string {
    if (this.selectedShiaijo < 0) {
      return this.translateService.instant('-');
    }
    return Tournament.SHIAIJO_NAMES[this.selectedShiaijo];
  }


  changeShiaijo(): void {
    this.selectedShiaijo++;
    if (this.tournament.shiaijos && this.selectedShiaijo >= this.tournament.shiaijos) {
      this.selectedShiaijo = -1;
    }
    this.resetFilter();
  }
}
