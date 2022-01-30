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
import {MessageService} from "../services/message.service";
import {BasicTableData} from "../basic/basic-table/basic-table-data";


@Component({
  selector: 'app-club-list',
  templateUrl: './club-list.component.html',
  styleUrls: ['./club-list.component.scss']
})
export class ClubListComponent implements OnInit {

  basicTableData: BasicTableData<Club> = new BasicTableData<Club>();

  @ViewChild(MatPaginator, {static: true}) paginator!: MatPaginator;
  @ViewChild(MatTable, {static: true}) table: MatTable<any>;
  @ViewChild(MatSort, {static: true}) sort!: MatSort;

  constructor(private clubService: ClubService, public dialog: MatDialog, private messageService: MessageService) {
    this.basicTableData.columns = ['id', 'name', 'country', 'city', 'address', 'email', 'phone', 'web'];
    this.basicTableData.columnsTags = ['idHeader', 'nameHeader', 'countryHeader', 'cityHeader', 'addressHeader', 'emailHeader', 'phoneHeader', 'webHeader'];
    this.basicTableData.visibleColumns = ['name', 'country', 'city'];
    this.basicTableData.selection = new SelectionModel<Club>(false, []);
    this.basicTableData.dataSource = new MatTableDataSource<Club>();
  }

  ngOnInit(): void {
    this.showAllElements();
  }

  showAllElements(): void {
    this.clubService.getAll().subscribe(clubs => {
      this.basicTableData.dataSource.data = clubs;
    });
  }

  addElement(): void {
    const club = new Club();
    this.openDialog('Add a new club', Action.Add, club);
  }

  editElement(): void {
    if (this.basicTableData.selectedElement) {
      this.openDialog('Edit club', Action.Update, this.basicTableData.selectedElement);
    }
  }

  deleteElement(): void {
    if (this.basicTableData.selectedElement) {
      this.openDialog('Delete club', Action.Delete, this.basicTableData.selectedElement);
    }
  }

  setSelectedItem(row: Club): void {
    if (row === this.basicTableData.selectedElement) {
      this.basicTableData.selectedElement = undefined;
    } else {
      this.basicTableData.selectedElement = row;
    }
  }

  openDialog(title: string, action: Action, club: Club) {
    const dialogRef = this.dialog.open(ClubDialogBoxComponent, {
      width: '250px',
      data: {title: title, action: action, entity: club}
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

  addRowData(club: Club) {
    this.clubService.add(club).subscribe(club => {
      this.basicTableData.dataSource.data.push(club);
      this.basicTableData.dataSource._updateChangeSubscription();
      this.messageService.infoMessage("Club Stored");
    });
  }

  updateRowData(club: Club) {
    this.clubService.update(club).subscribe(() => {
        this.messageService.infoMessage("Club Updated");
      }
    );
  }

  deleteRowData(club: Club) {
    this.clubService.delete(club).subscribe(() => {
        this.basicTableData.dataSource.data = this.basicTableData.dataSource.data.filter(existing_club => existing_club !== club);
        this.messageService.infoMessage("Club Deleted");
      }
    );
  }

}
