import {Component, ViewChild, OnInit} from '@angular/core';
import {MatPaginator} from '@angular/material/paginator';
import {MatTableDataSource} from '@angular/material/table';
import {MatTable} from '@angular/material/table';
import {MatSort} from '@angular/material/sort';
import {MatDialog} from '@angular/material/dialog';
import {SelectionModel} from "@angular/cdk/collections";
import {ClubService} from '../services/club.service';
import {Club} from '../models/club';
import {ClubDialogBoxComponent} from './club-dialog-box/club-dialog-box.component';
import {Action} from './club-dialog-box/club-dialog-box.component';


@Component({
  selector: 'app-club-list',
  templateUrl: './club-list.component.html',
  styleUrls: ['./club-list.component.scss']
})
export class ClubListComponent implements OnInit {

  columns: string[] = ['id', 'name', 'country', 'city', 'address', 'email', 'phone', 'web'];
  selection = new SelectionModel<Club>(false, []);
  clubs: Club[];
  dataSource: MatTableDataSource<Club>;
  selectedClub: Club;

  @ViewChild(MatPaginator, {static: true}) paginator!: MatPaginator;
  @ViewChild(MatTable, {static: true}) table: MatTable<any>;
  @ViewChild(MatSort, {static: true}) sort!: MatSort;

  constructor(private clubService: ClubService, public dialog: MatDialog) {
  }

  ngOnInit(): void {
    this.showAllClubs();
    this.dataSource = new MatTableDataSource<Club>(this.clubs);
    this.dataSource.paginator = this.paginator;
    this.dataSource.sort = this.sort;
  }

  showAllClubs(): void {
    this.clubService.getAll().subscribe(clubs => this.clubs = clubs);
  }

  addClub(): void {
    let club = new Club();
    this.openDialog('Add a new club', Action.Add, club);
    this.table.renderRows();
  }

  editClub(): void {
    this.openDialog('Edit club', Action.Update, this.selectedClub);
  }

  deleteClub(): void {
    this.openDialog('Delete club', Action.Delete, this.selectedClub);
  }

  setSelectedItem(row: Club): void {
    this.selectedClub = row;
  }

  openDialog(title: string, action: Action, club: Club) {
    const dialogRef = this.dialog.open(ClubDialogBoxComponent, {
      width: '250px',
      data: {title: title, action: action, entity: club}
    });

    dialogRef.afterClosed().subscribe(result => {
      console.log(result.event.action);
      if (result.event.action == Action.Add) {
        this.addRowData(result.data);
      } else if (result.event.action == Action.Update) {
        this.updateRowData(result.data);
      } else if (result.event.action == Action.Delete) {
        this.deleteRowData(result.data);
      }
    });
  }

  addRowData(club: Club) {
    this.clubService.add(club).subscribe(club => {
      this.clubs.push(club);
    });
    this.table.renderRows();
  }

  updateRowData(club: Club) {
    this.clubService.update(club).subscribe();
    this.table.renderRows();
  }

  deleteRowData(club: Club) {
    this.clubService.delete(club.id!);
    this.clubs = this.clubs.filter(existing_club => existing_club !== club);
    this.table.renderRows();
  }

}
