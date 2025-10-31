import {Component, HostListener, OnDestroy, OnInit} from '@angular/core';
import {MatDialog, MatDialogRef} from "@angular/material/dialog";
import {MessageService} from "../../services/message.service";
import {FightService} from "../../services/fight.service";
import {Fight} from "../../models/fight";
import {Tournament} from "../../models/tournament";
import {ActivatedRoute, Router} from "@angular/router";
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
import {EnvironmentService} from "../../environment.service";
import {MessageContent} from "../../websockets/message-content.model";
import {LoginService} from "../../services/login.service";
import {SenbatsuFightDialogBoxComponent} from "./senbatsu-fight-dialog-box/senbatsu-fight-dialog-box.component";
import {AudioService} from "../../services/audio.service";

@Component({
  selector: 'app-fight-list',
  templateUrl: './fight-list.component.html',
  styleUrls: ['./fight-list.component.scss']
})
export class FightListComponent extends RbacBasedComponent implements OnInit, OnDestroy {

  private websocketsPrefix: string = this.environmentService.getWebsocketPrefix();

  filteredFights: Map<number, Fight[]>;
  filteredUnties: Map<number, Duel[]>;
  //Check if a level label must be shown or not.
  filteredLevels: number[];

  selectedFight: Fight | undefined;
  selectedDuel: Duel | undefined;
  selectedGroup: Group | undefined | null;

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
  bubbleSortType: TournamentType = TournamentType.BUBBLE_SORT;
  showAvatars: boolean = false;

  resetFilterValue: Subject<boolean> = new Subject();

  resetTimerPosition: Subject<boolean> = new Subject();

  showLevelTags: boolean = false;
  showLevelOfGroup: Map<Group, boolean> = new Map<Group, boolean>;

  selectedShiaijo: number = -1;

  private topicSubscription: Subscription;


  constructor(private router: Router, private activatedRoute: ActivatedRoute,
              private tournamentService: TournamentService, private fightService: FightService,
              private environmentService: EnvironmentService,
              private groupService: GroupService, private duelService: DuelService,
              private timeChangedService: TimeChangedService, private duelChangedService: DuelChangedService,
              private untieAddedService: UntieAddedService, private groupUpdatedService: GroupUpdatedService,
              private dialog: MatDialog, private userSessionService: UserSessionService,
              private membersOrderChangedService: MembersOrderChangedService, private messageService: MessageService,
              rbacService: RbacService, private translateService: TranslateService,
              private systemOverloadService: SystemOverloadService,
              private rxStompService: RxStompService, private loginService: LoginService,
              private audioService: AudioService) {
    super(rbacService);
    this.filteredFights = new Map<number, Fight[]>();
    this.filteredUnties = new Map<number, Duel[]>();
    this.filteredLevels = [];
    this.groups = [];
    const state = this.router.getCurrentNavigation()?.extras.state;
    if (state) {
      //Send by previous view.
      if (state['tournamentId'] && !isNaN(Number(state['tournamentId']))) {
        this.tournamentId = Number(state['tournamentId']);
      } else {
        this.goBackToTournament();
      }
    } else {
      //Gets tournament from URL parameter (from QR codes).
      this.tournamentId = Number(this.activatedRoute.snapshot.queryParamMap.get('tournamentId'));
      if (!this.tournamentId) {
        this.tournamentId = Number(localStorage.getItem('tournamentId'));
        debugger
      }
      if (!this.tournamentId || isNaN(this.tournamentId)) {
        this.goBackToTournament();
      }
    }
  }

  ngOnInit(): void {
    //If no user, try to log in with guest.
    if (this.loginService.getJwtValue()) {
      this.initializeData();
    } else {
      if (this.tournamentId) {
        this.loginService.setGuestUserSession(this.tournamentId, (): void => {
          this.initializeData();
        });
      } else {
        this.goBackToTournament();
      }
    }
  }

  override ngOnDestroy(): void {
    super.ngOnDestroy();
    this.topicSubscription?.unsubscribe();
  }

  initializeData(): void {
    this.swappedColors = this.userSessionService.getSwappedColors();
    this.swappedTeams = this.userSessionService.getSwappedTeams();
    this.systemOverloadService.isTransactionalBusy.next(true);
    if (this.tournamentId) {
      this.tournamentService.get(this.tournamentId).subscribe((tournament: Tournament): void => {
        this.tournament = tournament;
        if (localStorage.getItem('username') === "guest" && this.tournament.locked) {
          this.goBackToTournament();
        } else {
          this.refreshFights();
        }
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
      if (_fight && this.groups && this.tournament) {
        this.resetFilter();
        for (const group of this.groups) {
          for (const fight of group.fights) {
            //Only this fight and the next ones. Not the previous ones.
            if (fight === _fight) {
              onlyNewFights = true;
            }
            if (onlyNewFights) {
              if (this.updateTeamOrder(fight, _fight)) {
                updatedFights = true;
              }
            }
          }
        }
        if (updatedFights) {
          this.fightService.updateAll(this.getFights()).subscribe();
        }
      }
      this.systemOverloadService.isTransactionalBusy.next(false);
    });

    this.topicSubscription = this.rxStompService.watch(this.websocketsPrefix + '/fights').subscribe((message: Message): void => {
      const messageContent: MessageContent = JSON.parse(message.body);
      if (messageContent.topic == "Fight" && (!messageContent.session || messageContent.session !== localStorage.getItem('session'))) {
        const fight: Fight = JSON.parse(messageContent.payload);
        if (!messageContent.type || messageContent.type.toLowerCase() == "updated") {
          this.replaceFight(fight);
        } else if (messageContent.type.toLowerCase() == "created") {
          this.refreshFights();
        }
      }
    });
  }

  updateTeamOrder(fight: Fight, fightReordered: Fight): boolean {
    let updatedFights: boolean = false;
    for (let i = 0; i < this.tournament.teamSize; i++) {
      if (fight.duels[i] && !fight.duels[i].duration) {
        if (fight.team1.id === fightReordered.team1.id) {
          fight.duels[i].competitor1 = fightReordered.duels[i].competitor1;
        } else if (fight.team2.id === fightReordered.team1.id) {
          fight.duels[i].competitor2 = fightReordered.duels[i].competitor1;
        } else if (fight.team1.id === fightReordered.team2.id) {
          fight.duels[i].competitor1 = fightReordered.duels[i].competitor2;
        } else if (fight.team2.id === fightReordered.team2.id) {
          fight.duels[i].competitor2 = fightReordered.duels[i].competitor2;
        } else {
          continue;
        }
        this.duelChangedService.isDuelUpdated.next(fight.duels[i]);
        updatedFights = true;
      }
    }
    return updatedFights;
  }

  @HostListener('document:keypress', ['$event'])
  handleKeyboardEvent(event: KeyboardEvent): void {
    if (event.key === 't') {
      this.showTimer(!this.timer);
    }
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

  private replaceFight(fight: Fight): void {
    //Replace on filter
    for (let key of this.filteredFights.keys()) {
      let indexOfFight: number = this.filteredFights.get(key)!.findIndex((element: Fight): boolean => element.id === fight.id);
      if (indexOfFight >= 0) {
        this.filteredFights.get(key)![indexOfFight] = fight;
        break;
      }
    }
    //Replace on groups
    for (let group of this.groups) {
      let indexOfFight: number = group.fights.findIndex((element: Fight): boolean => element.id === fight.id);
      if (indexOfFight >= 0) {
        group.fights![indexOfFight] = fight;
        break;
      }
    }
    //Update selected fight
    if (this.selectedFight && this.selectedFight.id === fight.id) {
      this.selectedFight = fight;
      for (let duel of fight.duels) {
        if (this.selectedDuel?.id === duel.id) {
          this.selectedDuel = duel;
          this.duelChangedService.isDuelUpdated.next(duel);
          break;
        }
      }
    }
  }

  private getFights(): Fight[] {
    return this.groups.flatMap((group: Group) => group.fights);
  }

  private getUnties(): Duel[] {
    return this.groups.flatMap((group: Group) => group.unties)
  }

  private getFightsByShiaijo(shiaijo: number): Fight[] {
    return this.groups.filter(group => group.shiaijo == shiaijo || !shiaijo).flatMap((group: Group) => group.fights);
  }

  private getUntiesByShiaijo(shiaijo: number): Duel[] {
    return this.groups.filter(group => group.shiaijo == shiaijo || !shiaijo).flatMap((group: Group) => group.unties)
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

    this.selectFirstUnfinishedDuel();

    this.resetFilter();
  }

  private setLevelTagVisibility(sortedGroups: Group[]): void {
    const showedLevel: boolean [] = []
    this.showLevelOfGroup = new Map<Group, boolean>();
    for (let group of sortedGroups) {
      if (group.level >= showedLevel.length) {
        showedLevel.push(true);
        //Hide level label if it hasn't fights on any of its groups.
        const groupsOfLevelWithFights: Group[] = sortedGroups.filter((group: Group) => group.level == showedLevel.length - 1 && group.fights.length > 0);
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
        disableClose: false,
        restoreFocus: false,
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
      this.tournament.type === TournamentType.KING_OF_THE_MOUNTAIN || this.tournament.type === TournamentType.BUBBLE_SORT
      || this.tournament.type === TournamentType.SENBATSU) {
      dialogRef = this.dialog.open(LeagueGeneratorComponent, {
        width: '85vw',
        panelClass: 'pop-up-panel',
        data: {title: 'Create Fights', action: Action.Add, tournament: this.tournament}
      });
    } else if (this.tournament.type === TournamentType.CHAMPIONSHIP) {
      this.openBracketsManager();
    }

    if (dialogRef) {
      dialogRef.afterClosed().subscribe(result => {
        if (result == undefined) {
          //Do nothing
        } else if (result?.action === Action.Add) {
          this.createGroupFight(result.data);
        } else if (result?.action === Action.Update) {
          this.updateRowData(result.data);
        } else if (result?.action === Action.Delete) {
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

  deleteElement(): void {
    if (this.selectedFight || this.selectedDuel) {
      let dialogRef = this.dialog.open(ConfirmationDialogComponent, {
        disableClose: false
      });
      dialogRef.componentInstance.messageTag = "deleteFightWarning"
      if (this.selectedFight) {
        dialogRef.componentInstance.parameters = {
          team1: !this.swappedTeams ? (this.selectedFight?.team1.name) : (this.selectedFight?.team2.name),
          team2: !this.swappedTeams ? (this.selectedFight?.team2.name) : (this.selectedFight?.team1.name),
        }
      } else if (this.selectedDuel) {
        dialogRef.componentInstance.parameters = {
          team1: !this.swappedTeams ? (this.selectedDuel?.competitor1?.lastname) : (this.selectedDuel?.competitor2?.lastname),
          team2: !this.swappedTeams ? (this.selectedDuel?.competitor2?.lastname) : (this.selectedDuel?.competitor1?.lastname)
        }
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
            this.selectedFight = undefined;
            this.groupService.update(this.selectedGroup).subscribe((): void => {
              this.messageService.infoMessage("fightDeleted");
              this.refreshFights();
            });
          }
        }
      });
    }
  }

  openAddFightDialog(title: string, action: Action, fight: Fight, group: Group, afterFight: Fight | undefined): void {
    const horizontalTeams: boolean = this.tournament.type === TournamentType.SENBATSU;
    const grid: boolean = this.tournament.type !== TournamentType.SENBATSU;
    const height: string = horizontalTeams ? '550px' : '95vh';
    let dialogRef;
    if (this.tournament.type == TournamentType.SENBATSU) {
      dialogRef = this.dialog.open(SenbatsuFightDialogBoxComponent, {
        panelClass: 'pop-up-panel',
        width: '90vw',
        height: height,
        maxWidth: '1000px',
        restoreFocus: false,
        data: {
          action: Action.Add,
          entity: fight,
          group: group,
          previousFight: afterFight,
          tournament: this.tournament,
          swappedColors: this.swappedColors,
          swappedTeams: this.swappedTeams,
          horizontalTeams: horizontalTeams,
          grid: grid,
        }
      });
    } else {
      dialogRef = this.dialog.open(FightDialogBoxComponent, {
        panelClass: 'pop-up-panel',
        width: '90vw',
        height: height,
        maxWidth: '1000px',
        restoreFocus: false,
        data: {
          action: Action.Add,
          entity: fight,
          group: group,
          previousFight: afterFight,
          tournament: this.tournament,
          swappedColors: this.swappedColors,
          swappedTeams: this.swappedTeams,
          horizontalTeams: horizontalTeams,
          grid: grid,
        }
      });
    }
    dialogRef.afterClosed().subscribe(result => {
      if (result == undefined) {
        //Do nothing
      } else if (result?.action === Action.Add) {
        this.selectFirstUnfinishedDuel();
      } else if (result?.action === Action.Update) {
        this.updateRowData(result.data);
      } else if (result?.action === Action.Delete) {
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
    this.fightService.delete(fight).subscribe((): void => {
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
        panelClass: 'pop-up-panel',
        width: '85vw',
        restoreFocus: false,
        data: {tournament: this.tournament, group: this.selectedGroup, finished: fightsFinished}
      });
      dialogRef.afterClosed().subscribe(result => {
        if (result?.action === Action.Cancel) {
        }
      });
    }
  }

  showCompetitorsClassification(): void {
    this.dialog.open(CompetitorsRankingComponent, {
      panelClass: 'pop-up-panel',
      width: '85vw',
      data: {tournament: this.tournament}
    });
  }

  downloadPDF(): void {
    if (this.tournament && this.tournament.id) {
      this.fightService.getFightSummaryPDf(this.tournament.id).subscribe((pdf: Blob): void => {
        const blob: Blob = new Blob([pdf], {type: 'application/pdf'});
        const downloadURL: string = window.URL.createObjectURL(blob);

        const anchor = document.createElement("a");
        anchor.download = "Fight List - " + this.tournament.name + ".pdf";
        anchor.href = downloadURL;
        anchor.click();
      });
    }
  }

  showTimer(show: boolean): void {
    if (this.canStartFight(this.selectedDuel)) {
      this.timer = show;
      this.resetTimerPosition.next(show);
    }
  }

  setIpponScores(duel: Duel): void {
    //Put default points.
    if (duel.competitor1 !== null && duel.competitor2 == null) {
      duel.competitor1Score = [];
      duel.competitor1Score.push(Score.FUSEN_GACHI);
      duel.competitor1Score.push(Score.FUSEN_GACHI);
    } else if (duel.competitor2 !== null && duel.competitor1 == null) {
      duel.competitor2Score = [];
      duel.competitor2Score.push(Score.FUSEN_GACHI);
      duel.competitor2Score.push(Score.FUSEN_GACHI);
    }
  }

  removeIpponScores(duel: Duel): void {
    for (let i: number = 0; i < duel.competitor1Score.length; i++) {
      if (duel.competitor1Score[i] == Score.FUSEN_GACHI) {
        duel.competitor1Score = [];
      }
    }
    for (let i: number = 0; i < duel.competitor2Score.length; i++) {
      if (duel.competitor2Score[i] == Score.FUSEN_GACHI) {
        duel.competitor2Score = [];
      }
    }
  }

  canStartFight(duel: Duel | undefined): boolean {
    return duel != undefined && duel?.competitor1 !== null && duel?.competitor2 !== null;
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
        this.selectedGroup = this.getGroup(this.selectedDuel);
        let showClassification: boolean = true;
        if (this.selectedGroup != null) {
          // Senbatsu, has a limited number of fights
          if (this.tournament.type === TournamentType.SENBATSU) {
            if (this.getFights().length < this.groups[0].teams.length - 1) {
              this.addElement();
            } else {
              this.showFightsFinishedMessage();
            }
          } else {
            // Tournament, each group must have a winner. Show for each group the winners.
            if (this.tournament.type === TournamentType.CHAMPIONSHIP && Group.isFinished(this.selectedGroup)) {
              //Shows group classification. And if there is a tie score can be solved.
              this.showClassification();
              showClassification = false;
            }
          }
        }
        // King of the mountain. Generate infinite fights.
        if (!this.selectFirstUnfinishedDuel()) {
          this.generateNextFights(showClassification);
        }
      });
    }
    console.log("selectedGroup", this.selectedGroup);
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
          if (duel.id == selectedDuel!.id) {
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
    this.fightService.createNext(this.tournamentId!).subscribe((_fights: Fight[]): void => {
      //Null value means that fights are not created due to an existing draw score.
      if (_fights.length > 0) {
        this.refreshFights();
      } else {
        if (showClassification && this.tournament.type !== TournamentType.KING_OF_THE_MOUNTAIN
          && this.tournament.type !== TournamentType.BUBBLE_SORT && this.tournament.type !== TournamentType.SENBATSU) {
          this.showFightsFinishedMessage();
          this.showClassification();
        }
        this.finishTournament(new Date());
      }
    });
  }

  showClassification(): void {
    if ((this.tournament?.teamSize && this.tournament?.teamSize > 1) ||
      (this.tournament && (this.tournament.type === TournamentType.KING_OF_THE_MOUNTAIN || this.tournament.type === TournamentType.SENBATSU
        || this.tournament.type === TournamentType.BUBBLE_SORT || this.tournament.type === TournamentType.CHAMPIONSHIP))) {
      this.showTeamsClassification(true);
    } else {
      this.showCompetitorsClassification();
    }
  }

  showFightsFinishedMessage(): void {
    this.messageService.infoMessage("fightsEnded");
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
    const fights: Fight[] = this.getFightsByShiaijo(this.selectedShiaijo);
    const unties: Duel[] = this.getUntiesByShiaijo(this.selectedShiaijo);
    if (fights) {
      for (const fight of fights) {
        for (const duel of unties) {
          if (!duel.finished) {
            this.selectedFight = undefined;
            this.selectDuel(duel);
            return true;
          }
        }
        for (const duel of fight.duels) {
          if (!duel.finished) {
            this.selectedFight = fight;
            this.selectDuel(duel);
            return true;
          }
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
        this.filteredFights.set(group.id!, group.fights.filter((fight: Fight) =>
          fight != null && (this.selectedShiaijo < 0 || fight.shiaijo == this.selectedShiaijo) && (
            (fight.team1 ? fight.team1.name.normalize('NFD').replace(/\p{Diacritic}/gu, "").toLowerCase().includes(filter) : "") ||
            (fight.team2 ? fight.team2.name.normalize('NFD').replace(/\p{Diacritic}/gu, "").toLowerCase().includes(filter) : "") ||
            (fight.team1 && fight.team1.members ? fight.team1.members.some(user => user !== undefined && (user.lastname.normalize('NFD').replace(/\p{Diacritic}/gu, "").toLowerCase().includes(filter) ||
              user.name.normalize('NFD').replace(/\p{Diacritic}/gu, "").toLowerCase().includes(filter) ||
              (user.club ? user.club.name.normalize('NFD').replace(/\p{Diacritic}/gu, "").toLowerCase().includes(filter) : ""))) : "") ||
            (fight.team2 && fight.team2.members ? fight.team2.members.some(user => user !== undefined && (user.lastname.normalize('NFD').replace(/\p{Diacritic}/gu, "").toLowerCase().includes(filter) ||
              user.name.normalize('NFD').replace(/\p{Diacritic}/gu, "").toLowerCase().includes(filter) ||
              (user.club ? user.club.name.normalize('NFD').replace(/\p{Diacritic}/gu, "").toLowerCase().includes(filter) : ""))) : "")))
        );
      } else {
        this.filteredFights.set(group.id!, []);
      }

      if (group.unties) {
        this.filteredUnties.set(group.id!, group.unties.filter((duel: Duel) =>
          (this.selectedShiaijo < 0 || group.shiaijo == this.selectedShiaijo) && (
            (duel.competitor1 ? duel.competitor1!.lastname.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "").includes(filter) : "") ||
            (duel.competitor1 ? duel.competitor1!.name.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "").includes(filter) || duel.competitor1!.idCard.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "").includes(filter) : "") ||
            (duel.competitor1 && duel.competitor1!.club ? duel.competitor1!.club.name.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "").includes(filter) : "") ||

            (duel.competitor2 ? duel.competitor2!.lastname.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "").includes(filter) : "") ||
            (duel.competitor2 ? duel.competitor2!.name.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "").includes(filter) || duel.competitor2!.idCard.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "").includes(filter) : "") ||
            (duel.competitor2 && duel.competitor2!.club ? duel.competitor2!.club.name.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "").includes(filter) : ""))));
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

  protected readonly TournamentType = TournamentType;

  playWhistle() {
    this.audioService.playWhistle();
  }

  stopWhistle() {
    this.audioService.stopWhistle();
  }
}
