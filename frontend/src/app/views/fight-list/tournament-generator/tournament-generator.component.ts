import {Component, OnInit, ViewChild} from '@angular/core';
import {Router} from "@angular/router";
import {RbacService} from "../../../services/rbac/rbac.service";
import {RbacBasedComponent} from "../../../components/RbacBasedComponent";
import {Tournament} from "../../../models/tournament";
import {TournamentService} from "../../../services/tournament.service";
import {
  TournamentBracketsEditorComponent
} from "../../../components/tournament-brackets-editor/tournament-brackets-editor.component";
import {Fight} from "../../../models/fight";
import {FightService} from "../../../services/fight.service";
import {MessageService} from "../../../services/message.service";
import {GroupService} from "../../../services/group.service";
import {Group} from "../../../models/group";
import {TournamentExtendedProperty} from "../../../models/tournament-extended-property.model";
import {TournamentExtraPropertyKey} from "../../../models/tournament-extra-property-key";
import {TournamentExtendedPropertiesService} from "../../../services/tournament-extended-properties.service";
import {TournamentType} from "../../../models/tournament-type";
import {NumberOfWinnersUpdatedService} from "../../../services/notifications/number-of-winners-updated.service";
import {
  TournamentChangedService
} from "../../../components/tournament-brackets-editor/tournament-brackets/tournament-changed.service";
import {BiitProgressBarType} from "@biit-solutions/wizardry-theme/info";

@Component({
  selector: 'app-tournament-generator',
  templateUrl: './tournament-generator.component.html',
  styleUrls: ['./tournament-generator.component.scss']
})
export class TournamentGeneratorComponent extends RbacBasedComponent implements OnInit {

  @ViewChild(TournamentBracketsEditorComponent)
  tournamentBracketsEditorComponent: TournamentBracketsEditorComponent;

  groupsDisabled: boolean = true;

  tournamentId: number;
  tournament: Tournament;
  isWizardEnabled: boolean = true;
  groups: Group[];
  groupsLevelZero: Group[] = [];
  totalTeams: number;

  numberOfWinners: number = 1;
  protected updatingGroup: boolean = false;
  protected generateGroupConfirmation: boolean = false;
  loadingGlobal: boolean = false;

  constructor(private router: Router, rbacService: RbacService, private tournamentService: TournamentService,
              private fightService: FightService, private messageService: MessageService,
              private groupService: GroupService, private tournamentChangedService: TournamentChangedService,
              private tournamentExtendedPropertiesService: TournamentExtendedPropertiesService,
              private numberOfWinnersUpdatedService: NumberOfWinnersUpdatedService) {
    super(rbacService);
    const state = this.router.getCurrentNavigation()?.extras.state;
    if (state) {
      if (state['tournamentId'] && !isNaN(Number(state['tournamentId']))) {
        this.tournamentId = Number(state['tournamentId']);
      } else {
        this.goBackToFights();
      }
      this.groupsDisabled = state['editionDisabled'];
    } else {
      this.goBackToFights();
    }
  }

  ngOnInit(): void {
    this.tournamentService.get(this.tournamentId).subscribe((tournament: Tournament): void => {
      this.tournament = tournament;
      this.tournamentChangedService.isTournamentChanged.next(tournament);
      this.refreshWinner();
    });
  }

  goBackToFights(): void {
    this.router.navigate(['/tournaments/fights'], {state: {tournamentId: this.tournamentId}});
  }

  addGroup(): void {
    if (this.groupsLevelZero.length < this.totalTeams / 2 && !this.updatingGroup) {
      this.updatingGroup = true;
      this.tournamentBracketsEditorComponent.addGroup();
    }
  }

  deleteGroup(): void {
    if (!this.updatingGroup) {
      this.updatingGroup = true;
      this.tournamentBracketsEditorComponent.deleteLast();
    }
  }

  generateElements(): void {
    this.loadingGlobal = true;
    this.fightService.create(this.tournamentId, 0).subscribe((fights: Fight[]): void => {
      this.messageService.infoMessage("infoFightCreated");
      this.goBackToFights();
    }).add(() => this.loadingGlobal = false);
  }

  groupsUpdated(groups: Group[]): void {
    this.groups = groups;
    this.groupsLevelZero = this.groups.filter((g: Group): boolean => {
      return g.level === 0;
    });
  }

  groupsActionsDisabled(disabled: boolean) {
    this.updatingGroup = false;
  }

  teamsSizeUpdated(totalTeams: number): void {
    this.totalTeams = totalTeams;
  }

  downloadPDF(): void {
    if (this.tournament?.id) {
      this.groupService.getGroupsByTournament(this.tournament.id).subscribe((pdf: Blob): void => {
        const blob: Blob = new Blob([pdf], {type: 'application/pdf'});
        const downloadURL: string = window.URL.createObjectURL(blob);

        const anchor: HTMLAnchorElement = document.createElement("a");
        anchor.download = "Group List - " + this.tournament.name + ".pdf";
        anchor.href = downloadURL;
        anchor.click();
      });
    }
  }

  private refreshWinner(): void {
    if (this.tournament.type == TournamentType.CHAMPIONSHIP) {
      this.tournamentExtendedPropertiesService.getByTournament(this.tournament).subscribe((_tournamentSelection: TournamentExtendedProperty[]): void => {
        if (_tournamentSelection) {
          for (const _tournamentProperty of _tournamentSelection) {
            if (_tournamentProperty.propertyKey == TournamentExtraPropertyKey.NUMBER_OF_WINNERS) {
              this.numberOfWinners = Number(_tournamentProperty.propertyValue.toLowerCase());
            }
          }
        }
        if (this.numberOfWinners == undefined) {
          this.numberOfWinners = 1;
        }
      });
    }
  }

  changeNumberOfWinners(numberOfWinners: number): void {
    this.numberOfWinners = numberOfWinners;

    const tournamentProperty: TournamentExtendedProperty = new TournamentExtendedProperty();
    tournamentProperty.tournament = this.tournament;
    tournamentProperty.propertyValue = numberOfWinners + "";
    tournamentProperty.propertyKey = TournamentExtraPropertyKey.NUMBER_OF_WINNERS;
    this.tournamentService.setNumberOfWinners(this.tournament, numberOfWinners).subscribe((): void => {
      this.numberOfWinnersUpdatedService.numberOfWinners.next(numberOfWinners);
      this.messageService.infoMessage('infoTournamentUpdated');
    });
  }

  refreshGroups() {
    this.loadingGlobal = true;
    this.groupService.refreshNonStartedGroups(this.tournamentId, 1).subscribe((): void => {
      this.tournamentBracketsEditorComponent.updateData(true, true);
    }).add(() => this.loadingGlobal = false);
  }

  protected readonly BiitProgressBarType = BiitProgressBarType;
}
