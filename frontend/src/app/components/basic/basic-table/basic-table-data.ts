import {SelectionModel} from "@angular/cdk/collections";
import {MatTableDataSource} from "@angular/material/table";

export class BasicTableData<T> {
  columns: string[];
  columnsTags: string[];
  columnsParameters: string[];
  visibleColumns: string[];
  selection: SelectionModel<T> = new SelectionModel<T>(false, []);
  dataSource: MatTableDataSource<T>;
  selectedElement: T | undefined;

  selectItem(item: any): void {
    this.selection.toggle(item);
    this.selectedElement = item;
  }

}
