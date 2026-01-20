import {Component, ElementRef, EventEmitter, Input, OnDestroy, OnInit, Output, ViewChild} from '@angular/core';
import {Group} from "../../models/group";
import {CdkDragDrop, transferArrayItem} from "@angular/cdk/drag-drop";
import {Team} from "../../models/team";
import {TeamListData} from "../basic/team-list/team-list-data";
import {TeamService} from "../../services/team.service";
import {Tournament} from "../../models/tournament";
import {GroupLink} from "../../models/group-link.model";
import {GroupLinkService} from "../../services/group-link.service";
import {GroupService} from "../../services/group.service";
import {RbacActivity} from "../../services/rbac/rbac.activity";
import {RbacService} from "../../services/rbac/rbac.service";
import {SystemOverloadService} from "../../services/notifications/system-overload.service";
import {GroupsUpdatedService} from "./tournament-brackets/groups-updated.service";
import {forkJoin, Observable, Subscription} from "rxjs";
import jsPDF from 'jspdf';
import domToImage from 'dom-to-image';
import {TournamentBracketsComponent} from "./tournament-brackets/tournament-brackets.component";
import {NumberOfWinnersUpdatedService} from "../../services/notifications/number-of-winners-updated.service";
import {random} from "../../utils/random/random";
import {MatDialog} from "@angular/material/dialog";
import {BracketsMeasures} from "./tournament-brackets/brackets-measures";
import {Message} from "@stomp/stompjs/esm6";
import {MessageContent} from "../../websockets/message-content.model";
import {RxStompService} from "../../websockets/rx-stomp.service";
import {EnvironmentService} from "../../environment.service";
import {TournamentChangedService} from "./tournament-brackets/tournament-changed.service";
import {BiitProgressBarType} from "@biit-solutions/wizardry-theme/info";

@Component({
  selector: 'tournament-brackets-editor',
  templateUrl: './tournament-brackets-editor.component.html',
  styleUrls: ['./tournament-brackets-editor.component.scss']
})
export class TournamentBracketsEditorComponent implements OnInit, OnDestroy {

  private websocketsPrefix: string = this.environmentService.getWebsocketPrefix();

  @Input()
  tournament: Tournament;

  @Input()
  droppingDisabled: boolean;

  @Input()
  groupsDisabled: boolean = true;

  @Output()
  onSelectedGroup: EventEmitter<Group> = new EventEmitter();

  @Output()
  onGroupsUpdated: EventEmitter<Group[]> = new EventEmitter();

  @Output()
  onGroupsDisabled: EventEmitter<boolean> = new EventEmitter();

  @Output()
  onTeamsLengthUpdated: EventEmitter<number> = new EventEmitter();

  @ViewChild('tournamentBracketsComponent', {read: ElementRef})
  public tournamentBracketsComponent: ElementRef;

  groups: Group[];
  selectedGroup: Group;

  //Level -> Src Group -> Dst Group
  relations: Map<number, { src: number, dest: number }[]>;
  teamListData: TeamListData = new TeamListData();
  totalTeams: number;
  numberOfWinnersFirstLevel: number;

  removeAllTeamsConfirmation: boolean = false;

  private topicSubscription: Subscription;

  loadingGlobal: boolean = false;

  protected readonly RbacActivity = RbacActivity;
  protected readonly BiitProgressBarType = BiitProgressBarType;

  constructor(private teamService: TeamService, private groupService: GroupService, private groupLinkService: GroupLinkService,
              public rbacService: RbacService, private systemOverloadService: SystemOverloadService, private dialog: MatDialog,
              private groupsUpdatedService: GroupsUpdatedService, private numberOfWinnersUpdatedService: NumberOfWinnersUpdatedService,
              private rxStompService: RxStompService, private environmentService: EnvironmentService,
              private tournamentChangedService: TournamentChangedService) {
  }

  ngOnInit(): void {
    this.groupsUpdatedService.areTeamListUpdated.subscribe((): void => {
      this.updateData(true, false);
    });
    this.numberOfWinnersUpdatedService.numberOfWinners.subscribe((numberOfWinners: number): void => {
      this.numberOfWinnersFirstLevel = numberOfWinners;
      this.updateData(true, false);
    });
    this.topicSubscription = this.rxStompService.watch(this.websocketsPrefix + '/groups').subscribe((message: Message): void => {
      const messageContent: MessageContent = JSON.parse(message.body);
      if (messageContent.topic == "Group" && (!messageContent.session || messageContent.session !== localStorage.getItem('session'))) {
        this.updateData(false, messageContent.actor == localStorage.getItem('username'));
      }
    });
    this.tournamentChangedService.isTournamentChanged.subscribe((_tournament: Tournament): void => {
      this.tournament = _tournament;
      if (_tournament) {
        this.updateData(true, false);
      }
    })
  }

  ngOnDestroy(): void {
    this.topicSubscription?.unsubscribe();
  }

  updateData(showBusy: boolean, ownAction: boolean): void {
    this.systemOverloadService.isBusy.next(showBusy);
    if (this.tournament?.id) {
      const teamsRequest: Observable<Team[]> = this.teamService.getFromTournament(this.tournament);
      const groupsRequest: Observable<Group[]> = this.groupService.getFromTournament(this.tournament.id);
      const relationsRequest: Observable<GroupLink[]> = this.groupLinkService.getFromTournament(this.tournament.id);

      forkJoin([teamsRequest, groupsRequest, relationsRequest]).subscribe(([_teams, _groups, _groupRelations]): void => {
        if (_teams) {
          _teams.sort(function (a: Team, b: Team) {
            return a.name.localeCompare(b.name);
          });
        }

        this.groups = _groups;
        this.onGroupsUpdated.emit(_groups);
        this.onGroupsDisabled.emit(ownAction);
        this.groupsUpdatedService.areGroupsUpdated.next(_groups);
        const groupTeamsIds: number[] = _groups.flatMap((group: Group): Team[] => group.teams).map((t: Team): number => t.id!);
        this.onTeamsLengthUpdated.next(_teams.length);
        this.totalTeams = _teams.length;
        _teams = _teams.filter((item: Team): boolean => groupTeamsIds.indexOf(item.id!) === -1);

        this.teamListData.teams = _teams;
        this.groupsUpdatedService.areTotalTeamsNumberUpdated.next(this.totalTeams);
        this.teamListData.filteredTeams = _teams;

        this.relations = this.convert(_groupRelations);
        this.groupsUpdatedService.areRelationsUpdated.next(this.convert(_groupRelations));
      }).add(() => this.loadingGlobal = false);
    }
  }

  selectGroup(group: Group): void {
    if (this.rbacService.isAllowed(RbacActivity.SELECT_GROUP)) {
      this.selectedGroup = group;
      this.onSelectedGroup.emit(group);
    }
  }


  convert(groupRelations: GroupLink[]): Map<number, { src: number, dest: number, winner: number }[]> {
    const relations: Map<number, { src: number, dest: number, winner: number }[]> = new Map();
    if (groupRelations) {
      for (const groupLink of groupRelations) {
        if (!relations.get(groupLink.source.level!)) {
          relations.set(groupLink.source.level!, []);
        }
        relations.get(groupLink.source.level!)?.push({
          src: groupLink.source!.index!,
          dest: groupLink.destination!.index!,
          winner: groupLink.winner
        });
      }
    }
    return relations;
  }


  removeTeam(event: CdkDragDrop<Team[], any>): void {
    transferArrayItem(
      event.previousContainer.data,
      event.container.data,
      event.previousIndex,
      event.currentIndex,
    );
    this.groupService.deleteTeamsFromTournament(this.tournament.id!, this.teamListData.teams).subscribe();
    this.teamListData.filteredTeams.sort((a: Team, b: Team) => a.name.localeCompare(b.name));
    this.teamListData.teams.sort((a: Team, b: Team) => a.name.localeCompare(b.name));
  }

  addGroup(): void {
    this.systemOverloadService.isBusy.next(true);
    const group: Group = new Group();
    group.tournament = this.tournament;
    group.level = 0;
    group.index = this.groups.filter((g: Group): boolean => {
      return g.level === 0;
    }).length;
    this.groupService.addGroup(group).subscribe((_group: Group): void => {
      //Refresh all groups, also other levels that can change.
      this.updateData(true, false);
    });
  }

  deleteLast(): void {
    if (this.groups.length > 0) {
      const level0Groups: Group[] = this.groups.filter((g: Group): boolean => {
        return g.level === 0;
      })
      const lastGroup: Group = level0Groups.reduce((prev: Group, current: Group): Group => (prev.index > current.index) ?
        prev : current, level0Groups[level0Groups.length - 1]);
      this.deleteGroup(lastGroup);
    }
  }

  deleteGroup(group: Group | undefined | null): void {
    if (group) {
      this.systemOverloadService.isBusy.next(true);
      this.groupService.deleteGroup(group).subscribe((): void => {
        //Refresh all groups, also other levels that can change.
        this.updateData(true, false);
      });
    }
  }

  public downloadAsPdf(): void {
    const groupsByLevel: Map<number, Group[]> = TournamentBracketsComponent.convert(this.groups);
    const height: number = groupsByLevel.get(0)?.length! * BracketsMeasures.GROUP_SEPARATION + this.totalTeams * 100;
    //const width = Math.max(groupsByLevel.size!, 3) * 500 + 100;
    const width: number = (groupsByLevel.size + 1) * (BracketsMeasures.GROUP_WIDTH + BracketsMeasures.LEVEL_SEPARATION + 100);
    const orientation: "p" | "portrait" | "l" | "landscape" = "landscape";
    const imageUnit: "pt" | "px" | "in" | "mm" | "cm" | "ex" | "em" | "pc" = "px";
    const widthMM: number = this.getMM(width);
    const heightMM: number = this.getMM(height);
    const ratio: number = this.getRatio(widthMM, heightMM);
    domToImage.toPng(this.tournamentBracketsComponent.nativeElement, {
      width: width,
      height: height
    }).then((result: string): void => {
      const jsPdfOptions = {
        orientation: orientation,
        unit: imageUnit,
        format: "a4",
      };
      const pdf: jsPDF = new jsPDF(jsPdfOptions);
      pdf.addImage(result, 'PNG', 25, 25, widthMM * ratio, heightMM * ratio);
      pdf.save(this.tournament.name + '.pdf');
    }).catch((): void => {
    });
  }

  private getRatio(width: number, height: number): number {
    return 500 / height;
  }

  private getMM(pixels: number): number {
    return (pixels * 25.4) / (window.devicePixelRatio * 96);
  }

  sortedGroups(): void {
    let groups: Group[] = this.groups;
    //Select group from level 0.
    groups = groups.filter((group: Group): boolean => group.level == 0);

    let teams: Team[] = this.teamListData.teams;
    teams.sort((a: Team, b: Team): number => a.name > b.name ? 1 : -1);
    while (teams.length > 0) {
      //Get group with fewer teams.
      groups.sort((a: Group, b: Group): number => {
        if (a.teams.length === b.teams.length) {
          return a.index - b.index;
        }
        return a.teams.length - b.teams.length;
      });
      const selectedGroup: Group = groups[0];
      //Add team to group
      selectedGroup.teams.push(teams[0]);
      this.teamListData.teams.splice(this.teamListData.teams.indexOf(teams[0]), 1);
    }
    this.updateGroupsTeams(groups);
  }

  randomGroups(): void {
    let groups: Group[] = this.groups;
    //Select group from level 0.
    this.loadingGlobal = true;
    groups = groups.filter((group: Group): boolean => group.level == 0);
    while (this.teamListData.teams.length > 0) {
      const team: Team = this.getRandomTeam(this.teamListData.teams);
      if (team) {
        //Get group with fewer teams.
        groups.sort((a: Group, b: Group): number => {
          return a.teams.length - b.teams.length;
        });
        const selectedGroup: Group = groups[0];
        //Add team to group
        selectedGroup.teams.push(team);
        this.teamListData.teams.splice(this.teamListData.teams.indexOf(team), 1);
      }
    }

    this.updateGroupsTeams(groups);
  }

  updateGroupsTeams(groups: Group[]): void {
    //Send final teams
    let observables: Observable<any>[] = [];
    for (const group of groups) {
      observables.push(this.groupService.addTeamsToGroup(group.id!, group.teams));
    }
    //Ensure all groups are updated.
    forkJoin(observables)
      .subscribe((): void => {
        this.updateData(true, false);
      });
  }

  getRandomTeam(teams: Team[]): Team {
    return teams[Math.floor(random() * teams.length)];
  }

  removeAllTeams(): void {
    this.groupService.deleteAllTeamsFromTournament(this.tournament.id!).subscribe((_groups: Group[]): void => {
      this.groupsUpdatedService.areTeamListUpdated.next([]);
    })
  }
}
