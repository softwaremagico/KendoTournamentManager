import {Component, Input, OnInit, ViewChild} from '@angular/core';
import {Club} from "../../../models/club";
import {BasicTableData} from "./basic-table-data";
import {MatDialog} from "@angular/material/dialog";
import {MatPaginator} from '@angular/material/paginator';
import {MatSort} from "@angular/material/sort";

@Component({
  selector: 'basic-table',
  templateUrl: './basic-table.component.html',
  styleUrls: ['./basic-table.component.scss']
})
export class BasicTableComponent implements OnInit {

  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;

  @Input()
  basicTableData: BasicTableData<any>;

  constructor(public dialog: MatDialog) {
  }

  ngOnInit(): void {
    // This is intentional
  }

  ngAfterViewInit() {
    this.basicTableData.dataSource.paginator = this.paginator;
    this.basicTableData.dataSource.sort = this.sort;
  }

  setSelectedItem(row: Club): void {
    if (row === this.basicTableData.selectedElement) {
      this.basicTableData.selectedElement = undefined;
    } else {
      this.basicTableData.selectedElement = row;
    }
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
      for (let tableColumn of this.basicTableData.columns) {
        if (oldVisibleColumns.includes(tableColumn)) {
          this.basicTableData.visibleColumns.push(tableColumn);
        }
      }
    }
  }


}
