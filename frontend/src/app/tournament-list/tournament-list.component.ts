import {Component, OnInit, ViewChild} from '@angular/core';
import {BasicTableData} from "../basic/basic-table/basic-table-data";
import {Tournament} from "../models/tournament";
import {MatPaginator} from "@angular/material/paginator";
import {MatTable, MatTableDataSource} from "@angular/material/table";
import {MatSort} from "@angular/material/sort";
import {TournamentService} from "../services/tournament.service";
import {MatDialog} from "@angular/material/dialog";
import {MessageService} from "../services/message.service";
import {ClubService} from "../services/club.service";
import {SelectionModel} from "@angular/cdk/collections";
import {TournamentDialogBoxComponent} from "./tournament-dialog-box/tournament-dialog-box.component";
import {TournamentRolesComponent} from "./tournament-roles/tournament-roles.component";

export enum Action {
  Add,
  Update,
  Delete,
  Cancel
}

@Component({
  selector: 'app-tournament-list',
  templateUrl: './tournament-list.component.html',
  styleUrls: ['./tournament-list.component.scss']
})
export class TournamentListComponent implements OnInit {

  basicTableData: BasicTableData<Tournament> = new BasicTableData<Tournament>();

  @ViewChild(MatPaginator, {static: true}) paginator!: MatPaginator;
  @ViewChild(MatTable, {static: true}) table: MatTable<any>;
  @ViewChild(MatSort, {static: true}) sort!: MatSort;

  constructor(private tournamentService: TournamentService, public dialog: MatDialog, private messageService: MessageService) {
    this.basicTableData.columns = ['id', 'name', 'type', 'shiaijos', 'teamSize'];
    this.basicTableData.columnsTags = ['id', 'name', 'tournamentType', 'shiaijos', 'teamSize'];
    this.basicTableData.visibleColumns = ['name', 'type', 'teamSize'];
    this.basicTableData.selection = new SelectionModel<Tournament>(false, []);
    this.basicTableData.dataSource = new MatTableDataSource<Tournament>();
  }

  ngOnInit(): void {
    this.showAllElements();
  }

  showAllElements(): void {
    this.tournamentService.getAll().subscribe(tournaments => {
      this.basicTableData.dataSource.data = tournaments;
    });
  }

  addElement(): void {
    const tournament = new Tournament();
    this.openDialog('Add a tournament', Action.Add, tournament);
  }

  editElement(): void {
    if (this.basicTableData.selectedElement) {
      this.openDialog('Edit tournament', Action.Update, this.basicTableData.selectedElement);
    }
  }

  deleteElement(): void {
    if (this.basicTableData.selectedElement) {
      this.openDialog('Delete tournament', Action.Delete, this.basicTableData.selectedElement);
    }
  }

  setSelectedItem(row: Tournament): void {
    if (row === this.basicTableData.selectedElement) {
      this.basicTableData.selectedElement = undefined;
    } else {
      this.basicTableData.selectedElement = row;
    }
  }

  openDialog(title: string, action: Action, tournament: Tournament) {
    const dialogRef = this.dialog.open(TournamentDialogBoxComponent, {
      width: '250px',
      data: {
        title: title, action: action, entity: tournament
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result.action == Action.Add) {
        this.addRowData(result.data);
      } else if (result.action == Action.Update) {
        this.updateRowData(result.data);
      } else if (result.action == Action.Delete) {
        this.deleteRowData(result.data);
      }
    });
  }

  addRowData(tournament: Tournament) {
    this.tournamentService.add(tournament).subscribe(tournament => {
      this.basicTableData.dataSource.data.push(tournament);
      this.basicTableData.dataSource._updateChangeSubscription();
      this.messageService.infoMessage("Tournament Stored");
    });
  }

  updateRowData(tournament: Tournament) {
    this.tournamentService.update(tournament).subscribe(() => {
        this.messageService.infoMessage("Tournament Updated");
      }
    );
  }

  deleteRowData(tournament: Tournament) {
    this.tournamentService.delete(tournament).subscribe(() => {
        this.basicTableData.dataSource.data = this.basicTableData.dataSource.data.filter(existing_Tournament => existing_Tournament !== tournament);
        this.messageService.infoMessage("Tournament Deleted");
      }
    );
  }

  addRole(): void {
    if (this.basicTableData.selectedElement) {
      const dialogRef = this.dialog.open(TournamentRolesComponent, {
        data: {
          tournament: this.basicTableData.selectedElement
        }
      });
    }
  }

}

