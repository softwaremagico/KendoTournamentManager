import {Component, OnInit, ViewChild} from '@angular/core';
import {BasicTableData} from "../../components/basic/basic-table/basic-table-data";
import {Tournament} from "../../models/tournament";
import {MatPaginator} from "@angular/material/paginator";
import {MatTable, MatTableDataSource} from "@angular/material/table";
import {MatSort} from "@angular/material/sort";
import {TournamentService} from "../../services/tournament.service";
import {MatDialog} from "@angular/material/dialog";
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

@Component({
  selector: 'app-tournament-list',
  templateUrl: './tournament-list.component.html',
  styleUrls: ['./tournament-list.component.scss']
})
export class TournamentListComponent extends RbacBasedComponent implements OnInit {

  basicTableData: BasicTableData<Tournament> = new BasicTableData<Tournament>();

  @ViewChild(MatPaginator, {static: true}) paginator!: MatPaginator;
  @ViewChild(MatTable, {static: true}) table: MatTable<any>;
  @ViewChild(MatSort, {static: true}) sort!: MatSort;

  constructor(private router: Router, private userSessionService: UserSessionService, private tournamentService: TournamentService,
              private rankingService: RankingService, private translateService: TranslateService, public dialog: MatDialog,
              private messageService: MessageService, rbacService: RbacService) {
    super(rbacService);
    this.basicTableData.columns = ['id', 'name', 'type', 'scoreRules', 'locked', 'shiaijos', 'teamSize', 'createdAt', 'createdBy', 'updatedAt', 'updatedBy'];
    this.basicTableData.columnsTags = ['id', 'name', 'tournamentType', 'scoreRules', 'locked', 'shiaijos', 'teamSize', 'createdAt', 'createdBy', 'updatedAt', 'updatedBy'];
    this.basicTableData.visibleColumns = ['name', 'type', 'teamSize'];
    this.basicTableData.dataSource = new MatTableDataSource<Tournament>();
  }

  ngOnInit(): void {
    this.showAllElements();
  }

  showAllElements(): void {
    this.tournamentService.getAll().subscribe(tournaments => {
      this.basicTableData.dataSource.data = tournaments;
      //Select session tournament.
      const selectedTournament: Tournament = this.basicTableData.dataSource.data.filter(x => x.id == Number(this.userSessionService.getTournament()))[0];
      const selectedElements: Tournament[] = [];
      selectedElements.push(selectedTournament);
      this.basicTableData.selection = new SelectionModel<Tournament>(false, selectedElements);
      this.basicTableData.selectedElement = selectedTournament;
    });
  }

  addElement(): void {
    const tournament = new Tournament();
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

  openDialog(title: string, action: Action, tournament: Tournament) {
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
        this.userSessionService.setTournament(undefined);
      }
    });
  }

  addRowData(tournament: Tournament) {
    this.tournamentService.add(tournament).subscribe(_tournament => {
      this.basicTableData.dataSource.data.push(_tournament);
      this.basicTableData.dataSource._updateChangeSubscription();
      this.basicTableData.selectItem(_tournament);
      this.messageService.infoMessage('infoTournamentStored');
    });
  }

  updateRowData(tournament: Tournament) {
    this.tournamentService.update(tournament).subscribe(() => {
        this.messageService.infoMessage('infoTournamentUpdated');
      }
    );
  }

  deleteRowData(tournament: Tournament) {
    this.tournamentService.delete(tournament).subscribe(() => {
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
      this.userSessionService.setTournament(this.basicTableData.selectedElement.id + "");
      this.router.navigate(['/tournaments/fights'], {state: {tournamentId: this.basicTableData.selectedElement.id}});
    }
  }

  downloadBlogCode() {
    if (this.basicTableData.selectedElement && this.basicTableData.selectedElement.id) {
      this.rankingService.getTournamentSummaryAsHtml(this.basicTableData.selectedElement.id).subscribe((html: Blob) => {
        const blob = new Blob([html], {type: 'txt/plain'});
        const downloadURL = window.URL.createObjectURL(blob);

        const anchor = document.createElement("a");
        anchor.download = "Code - " + this.basicTableData.selectedElement!.name + ".txt";
        anchor.href = downloadURL;
        anchor.click();
      });
    }
  }

  downloadAccreditations() {
    if (this.basicTableData.selectedElement && this.basicTableData.selectedElement.id) {
      this.tournamentService.getAccreditations(this.basicTableData.selectedElement.id).subscribe((html: Blob) => {
        const blob = new Blob([html], {type: 'application/pdf'});
        const downloadURL = window.URL.createObjectURL(blob);

        const anchor = document.createElement("a");
        anchor.download = "Accreditations - " + this.basicTableData.selectedElement!.name + ".pdf";
        anchor.href = downloadURL;
        anchor.click();
      });
    }
  }

  downloadDiplomas() {
    if (this.basicTableData.selectedElement && this.basicTableData.selectedElement.id) {
      this.tournamentService.getDiplomas(this.basicTableData.selectedElement.id).subscribe((html: Blob) => {
        const blob = new Blob([html], {type: 'application/pdf'});
        const downloadURL = window.URL.createObjectURL(blob);

        const anchor = document.createElement("a");
        anchor.download = "Diplomas - " + this.basicTableData.selectedElement!.name + ".pdf";
        anchor.href = downloadURL;
        anchor.click();
      });
    }
  }

  lockElement(locked: boolean): void {
    if (this.basicTableData.selectedElement) {
      this.basicTableData.selectedElement.locked = locked;
      this.updateRowData(this.basicTableData.selectedElement);
    }
  }

  disableRow(argument: any): boolean {
    return (argument as Tournament).locked;
  }
}

