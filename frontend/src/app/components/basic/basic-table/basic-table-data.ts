import {SelectionModel} from "@angular/cdk/collections";
import {MatTableDataSource} from "@angular/material/table";
import {DatabaseObject} from "../../../models/database-object";

export class BasicTableData<T extends DatabaseObject> {
  columns: string[];
  columnsTags: string[];
  columnsParameters: string[];
  visibleColumns: string[];
  selection: SelectionModel<T> = new SelectionModel<T>(false, []);
  dataSource: MatTableDataSource<T>;
  selectedElement: T | undefined;
  element: string;

  constructor(element: string) {
    this.element = element;
  }

  selectItem(item: any): void {
    //We select by index and not by item as not always the item is the entity on the datasource.
    let selectedItem: number = this.dataSource.data.findIndex((obj: T): boolean => obj.id === item.id);
    if (selectedItem >= 0) {
      item = this.dataSource.data[selectedItem];
    }
    this.selection.toggle(item);
  }

  hasSelectedItem(): boolean {
    return this.selectedElement !== undefined;
  }

}
