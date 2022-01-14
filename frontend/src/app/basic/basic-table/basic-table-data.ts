import {SelectionModel} from "@angular/cdk/collections";
import {MatTableDataSource} from "@angular/material/table";

export class BasicTableData<T> {
  columns: string[];
  columnsTags: string[];
  columnsParameters: string[];
  visibleColumns: string[];
  selection = new SelectionModel<T>(false, []);
  dataSource: MatTableDataSource<T>;
  selectedElement: T | undefined;

  constructor(columns: string[], columnsTags: string[], visibleColumns: string[]) {
  }

}
