import {Component, OnInit, ViewChild} from '@angular/core';
import {BasicTableData} from "../../components/basic/basic-table/basic-table-data";
import {Tournament} from "../../models/tournament";
import {MatPaginator} from "@angular/material/paginator";
import {MatTable, MatTableDataSource} from "@angular/material/table";
import {MatSort} from "@angular/material/sort";
import {TournamentService} from "../../services/tournament.service";
import {MatDialog, MatDialogRef} from "@angular/material/dialog";
import {MessageService} from "../../services/message.service";
import {SelectionModel} from "@angular/cdk/collections";
import {TournamentDialogBoxComponent} from "./tournament-dialog-box/tournament-dialog-box.component";
import {TournamentRolesComponent} from "./tournament-roles/tournament-roles.component";
import {TournamentTeamsComponent} from "./tournament-teams/tournament-teams.component";

import {Router} from '@angular/router';
import {UserSessionService} from "../../services/user-session.service";
import {Action} from "../../action";
import {RankingService} from "../../services/ranking.service";
import {TranslateService} from "@ngx-translate/core";
import {RbacService} from "../../services/rbac/rbac.service";
import {RbacBasedComponent} from "../../components/RbacBasedComponent";
import {
  RoleSelectorDialogBoxComponent
} from "../../components/role-selector-dialog-box/role-selector-dialog-box.component";
import {SystemOverloadService} from "../../services/notifications/system-overload.service";
import {AchievementsService} from "../../services/achievements.service";
import {ConfirmationDialogComponent} from "../../components/basic/confirmation-dialog/confirmation-dialog.component";

@Component({
  selector: 'app-tournament-list',
  templateUrl: './tournament-list.component.html',
  styleUrls: ['./tournament-list.component.scss']
})
export class TournamentListComponent extends RbacBasedComponent implements OnInit {

  basicTableData: BasicTableData<Tournament> = new BasicTableData<Tournament>("Tournament");

  @ViewChild(MatPaginator, {static: true}) paginator!: MatPaginator;
  @ViewChild(MatTable, {static: true}) table: MatTable<any>;
  @ViewChild(MatSort, {static: true}) sort!: MatSort;

  constructor(private router: Router, private userSessionService: UserSessionService, private tournamentService: TournamentService,
              private rankingService: RankingService, private translateService: TranslateService, public dialog: MatDialog,
              private messageService: MessageService, rbacService: RbacService, private systemOverloadService: SystemOverloadService,
              private achievementsService: AchievementsService) {
    super(rbacService);
    this.basicTableData.columns = ['id', 'name', 'type', 'tournamentScore', 'locked', 'shiaijos', 'teamSize', 'createdAt', 'createdBy', 'updatedAt', 'updatedBy'];
    this.basicTableData.columnsTags = ['id', 'name', 'tournamentType', 'scoreRules', 'locked', 'shiaijos', 'teamSize', 'createdAt', 'createdBy', 'updatedAt', 'updatedBy'];
    this.basicTableData.visibleColumns = ['name', 'type', 'teamSize'];
    this.basicTableData.dataSource = new MatTableDataSource<Tournament>();
  }

  ngOnInit(): void {
    this.showAllElements();
  }

  showAllElements(): void {
    this.systemOverloadService.isTransactionalBusy.next(true);
    this.tournamentService.getAll().subscribe((_tournaments: Tournament[]): void => {
      if (_tournaments) {
        const tournaments: Tournament[] = [];
        for (let _tournament of _tournaments) {
          if (_tournament) {
            tournaments.push(Tournament.clone(_tournament));
          }
        }
        this.basicTableData.dataSource.data = tournaments;
        //Select session tournament.
        const selectedTournament: Tournament = this.basicTableData.dataSource.data.filter((tournament: Tournament): boolean =>
          tournament.id == Number(this.userSessionService.getSelectedTournament()))[0];
        const selectedElements: Tournament[] = [];
        selectedElements.push(selectedTournament);
        this.basicTableData.selection = new SelectionModel<Tournament>(false, selectedElements);
        this.basicTableData.selectedElement = selectedTournament;
        this.systemOverloadService.isTransactionalBusy.next(false);
      }
    });
  }

  addElement(): void {
    const tournament: Tournament = new Tournament();
    tournament.duelsDuration = Tournament.DEFAULT_DUELS_DURATION;
    tournament.type = Tournament.DEFAULT_TYPE;
    tournament.shiaijos = Tournament.DEFAULT_SHIAIJOS;
    tournament.teamSize = Tournament.DEFAULT_TEAM_SIZE;
    this.openDialog(this.translateService.instant('competitionAdd'), Action.Add, tournament);
  }

  editElement(): void {
    if (this.basicTableData.selectedElement) {
      this.openDialog(this.translateService.instant('competitionEdit'), Action.Update, this.basicTableData.selectedElement);
    }
  }

  deleteElement(): void {
    if (this.basicTableData.selectedElement) {
      this.openDialog(this.translateService.instant('competitionDelete'), Action.Delete, this.basicTableData.selectedElement);
    }
  }

  openDialog(title: string, action: Action, tournament: Tournament): void {
    const dialogRef = this.dialog.open(TournamentDialogBoxComponent, {
      width: '600px',
      data: {
        title: title, action: action, entity: tournament
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result == undefined) {
        //Do nothing
      } else if (result.action == Action.Add) {
        this.addRowData(result.data);
      } else if (result.action == Action.Update) {
        this.updateRowData(result.data);
      } else if (result.action == Action.Delete) {
        this.deleteRowData(result.data);
        this.userSessionService.setSelectedTournament(undefined);
      }
    });
  }

  addRowData(tournament: Tournament): void {
    this.tournamentService.add(tournament).subscribe((_tournament: Tournament): void => {
      //If data is not already added though table webservice.
      if (this.basicTableData.dataSource.data.findIndex((obj: Tournament): boolean => obj.id === _tournament.id) < 0) {
        this.basicTableData.dataSource.data.push(_tournament);
        this.basicTableData.dataSource._updateChangeSubscription();
      }
      this.basicTableData.selectItem(_tournament);
      this.messageService.infoMessage('infoTournamentStored');
    });
  }

  updateRowData(tournament: Tournament): void {
    this.tournamentService.update(tournament).subscribe((): void => {
        this.messageService.infoMessage('infoTournamentUpdated');
        this.basicTableData.selectedElement = tournament;
      }
    );
  }

  deleteRowData(tournament: Tournament): void {
    this.tournamentService.delete(tournament).subscribe((): void => {
        this.basicTableData.dataSource.data = this.basicTableData.dataSource.data.filter(existing_Tournament => existing_Tournament !== tournament);
        this.messageService.infoMessage('infoTournamentDeleted');
        this.basicTableData.selectedElement = undefined;
      }
    );
  }

  addRoles(): void {
    if (this.basicTableData.selectedElement) {
      this.dialog.open(TournamentRolesComponent, {
        data: {
          tournament: this.basicTableData.selectedElement
        }
      });
    }
  }

  addTeams(): void {
    if (this.basicTableData.selectedElement) {
      this.dialog.open(TournamentTeamsComponent, {
        data: {
          tournament: this.basicTableData.selectedElement
        }
      });
    }
  }

  openFights(): void {
    if (this.basicTableData.selectedElement) {
      this.userSessionService.setSelectedTournament(this.basicTableData.selectedElement.id + "");
      this.router.navigate(['/tournaments/fights'], {state: {tournamentId: this.basicTableData.selectedElement.id}});
    }
  }

  downloadBlogCode(): void {
    if (this.basicTableData.selectedElement?.id) {
      this.rankingService.getTournamentSummaryAsHtml(this.basicTableData.selectedElement.id).subscribe((html: Blob): void => {
        const blob: Blob = new Blob([html], {type: 'txt/plain'});
        const downloadURL: string = window.URL.createObjectURL(blob);

        const anchor = document.createElement("a");
        anchor.download = "Code - " + this.basicTableData.selectedElement!.name + ".txt";
        anchor.href = downloadURL;
        anchor.click();
      });
    }
  }

  downloadAccreditations(): void {
    if (this.basicTableData.selectedElement) {
      const dialogRef: MatDialogRef<RoleSelectorDialogBoxComponent> = this.dialog.open(RoleSelectorDialogBoxComponent, {
        data: {
          tournament: this.basicTableData.selectedElement
        }
      });

      dialogRef.afterClosed().subscribe(result => {
        if (result.action !== Action.Cancel) {
          if (this.basicTableData.selectedElement?.id) {
            this.tournamentService.getAccreditations(this.basicTableData.selectedElement.id, result.newOnes, result.data).subscribe((html: Blob): void => {
              if (html !== null) {
                const blob: Blob = new Blob([html], {type: 'application/pdf'});
                const downloadURL: string = window.URL.createObjectURL(blob);

                const anchor: HTMLAnchorElement = document.createElement("a");
                anchor.download = "Accreditations - " + this.basicTableData.selectedElement!.name + ".pdf";
                anchor.href = downloadURL;
                anchor.click();
              } else {
                this.messageService.warningMessage('noResults');
              }
            });
          }
        }
      });
    }
  }

  downloadDiplomas(): void {
    if (this.basicTableData.selectedElement) {
      const dialogRef: MatDialogRef<RoleSelectorDialogBoxComponent> = this.dialog.open(RoleSelectorDialogBoxComponent, {
        data: {
          tournament: this.basicTableData.selectedElement
        }
      });

      dialogRef.afterClosed().subscribe(result => {
        if (result.action !== Action.Cancel) {
          if (this.basicTableData.selectedElement?.id) {
            this.tournamentService.getDiplomas(this.basicTableData.selectedElement.id, result.newOnes, result.data).subscribe((html: Blob) => {
              if (html !== null) {
                const blob: Blob = new Blob([html], {type: 'application/pdf'});
                const downloadURL: string = window.URL.createObjectURL(blob);

                const anchor: HTMLAnchorElement = document.createElement("a");
                anchor.download = "Diplomas - " + this.basicTableData.selectedElement!.name + ".pdf";
                anchor.href = downloadURL;
                anchor.click();
              } else {
                this.messageService.warningMessage('noResults');
              }
            });
          }
        }
      });
    }
  }

  lockElement(locked: boolean): void {
    if (this.basicTableData.selectedElement) {
      this.basicTableData.selectedElement.locked = locked;
      if (locked) {
        this.achievementsService.regenerateTournamentAchievements(this.basicTableData.selectedElement?.id!).subscribe();
        if (!this.basicTableData.selectedElement.lockedAt) {
          this.basicTableData.selectedElement.lockedAt = new Date();
        }
        if (!this.basicTableData.selectedElement.finishedAt) {
          this.basicTableData.selectedElement.finishedAt = new Date();
        }
      }
      this.updateRowData(this.basicTableData.selectedElement);
    }
  }

  disableRow(argument: any): boolean {
    return (argument as Tournament).locked;
  }

  openStatistics(): void {
    if (this.basicTableData.selectedElement) {
      this.userSessionService.setSelectedTournament(this.basicTableData.selectedElement.id + "");
      this.router.navigate(['/tournaments/statistics'], {state: {tournamentId: this.basicTableData.selectedElement.id}});
    }
  }

  openCloneTournament(): void {
    if (this.basicTableData.selectedElement) {
      let dialogRef: MatDialogRef<ConfirmationDialogComponent> = this.dialog.open(ConfirmationDialogComponent, {
        disableClose: false
      });
      dialogRef.componentInstance.messageTag = "tournamentCloneWarning"

      dialogRef.afterClosed().subscribe(result => {
        if (result) {
          this.cloneElement();
        }
      });
    }
  }

  cloneElement(): void {
    const tournamentId: number = this.basicTableData.selectedElement?.id!;
    this.basicTableData.selectedElement = undefined;
    this.tournamentService.clone(tournamentId).subscribe((_tournament: Tournament): void => {
      this.basicTableData.dataSource.data.push(_tournament);
      this.basicTableData.selectItem(_tournament);
      this.basicTableData.dataSource._updateChangeSubscription();
      this.messageService.infoMessage('infoTournamentStored');
    });
  }

  downloadZip(): void {
    if (this.basicTableData.selectedElement?.id) {
      this.rankingService.getAllListAsZip(this.basicTableData.selectedElement.id).subscribe((html: Blob): void => {
        const blob: Blob = new Blob([html], {type: 'application/zip'});
        const downloadURL: string = window.URL.createObjectURL(blob);

        const anchor = document.createElement("a");
        anchor.download = this.basicTableData.selectedElement!.name + ".zip";
        anchor.href = downloadURL;
        anchor.click();
      });
    }
  }
}
