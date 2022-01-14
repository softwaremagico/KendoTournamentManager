import {Component, Input, OnInit} from '@angular/core';
import {SelectionModel} from "@angular/cdk/collections";
import {Club} from "../../models/club";
import {MatTableDataSource} from "@angular/material/table";
import {BasicTableData} from "./basic-table-data";
import {Action, ClubDialogBoxComponent} from "../../club-list/club-dialog-box/club-dialog-box.component";
import {MessageService} from "../../services/message.service";
import {MatDialog} from "@angular/material/dialog";

@Component({
  selector: 'basic-table',
  templateUrl: './basic-table.component.html',
  styleUrls: ['./basic-table.component.scss']
})
export class BasicTableComponent implements OnInit {

  @Input()
  basicTableData: BasicTableData<any>;

  constructor(public dialog: MatDialog, private messageService: MessageService) {
  }

  ngOnInit(): void {
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


  showAllElements(): void {
    this.clubService.getAll().subscribe(clubs => {
      this.basicTableData.dataSource.data = clubs;
    });
  }

  addRowData(club: Club) {
    this.clubService.add(club).subscribe(club => {
      this.basicTableData.dataSource.data.push(club);
      this.basicTableData.dataSource._updateChangeSubscription();
      this.messageService.infoMessage("clubStored");
    });
  }

  updateRowData(element: Club) {
    this.clubService.update(element).subscribe(club => {
        this.messageService.infoMessage("clubUpdated");
      }
    );
  }

  deleteRowData(element: Club) {
    this.clubService.delete(element).subscribe(n => {
        this.basicTableData.dataSource.data = this.basicTableData.dataSource.data.filter(existing_element => existing_element !== element);
        this.messageService.infoMessage("clubDeleted");
      }
    );
  }

  filter(event: Event) {
    const filter = (event.target as HTMLInputElement).value;
    this.basicTableData.dataSource.filter = filter.trim().toLowerCase();
  }

  isColumnVisible(column: string): boolean {
    return this.basicTableData.visibleColumns.includes(column);
  }

  toggleColumnVisibility(column: string) {
    const index: number = this.basicTableData.visibleColumns.indexOf(column);
    if (index !== -1) {
      this.basicTableData.visibleColumns.splice(index, 1);
    } else {
      let oldVisibleColumns: string[];
      oldVisibleColumns = [...this.basicTableData.visibleColumns];
      oldVisibleColumns.push(column);
      this.basicTableData.visibleColumns.length = 0;
      //Maintain columns order.
      for (let column of this.basicTableData.columns) {
        if (oldVisibleColumns.includes(column)) {
          this.basicTableData.visibleColumns.push(column);
        }
      }
    }
  }


}
