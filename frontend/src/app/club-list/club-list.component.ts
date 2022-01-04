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
  styleUrls: ['./club-list.component.css']
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
    // this.clubService.getAll()
    //   .subscribe(clubs => this.clubs = clubs);
    this.clubs = [
      {id: 1, name: 'Club1', country: 'Spain', city: 'Alcira'},
      {id: 2, name: 'Club2', country: 'Spain', city: 'Torremolinos'}
    ];
  }

  addClub(): void {
    let club = new Club();
    this.openDialog(Action.Add, club);
    this.table.renderRows();
  }

  editClub(): void {
    this.openDialog(Action.Update, this.selectedClub);
    this.table.renderRows();
  }

  deleteClub(): void {
    this.table.renderRows();
  }

  setSelectedItem(row: Club): void {
    this.selectedClub = row;
  }

  openDialog(action: Action, club: Club) {
    const dialogRef = this.dialog.open(ClubDialogBoxComponent, {
      width: '250px',
      data: club
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result.event == 'Add') {
        // this.addRowData(result.data);
      } else if (result.event == 'Update') {
        // this.updateRowData(result.data);
      } else if (result.event == 'Delete') {
        // this.deleteRowData(result.data);
      }
    });
  }

}
