import {Component, OnInit, ViewChild} from '@angular/core';
import {BasicTableData} from "../../basic/basic-table/basic-table-data";
import {Club} from "../../models/club";
import {MatPaginator} from "@angular/material/paginator";
import {MatTable, MatTableDataSource} from "@angular/material/table";
import {MatSort} from "@angular/material/sort";
import {User} from "../../models/user";
import {ClubService} from "../../services/club.service";
import {MatDialog} from "@angular/material/dialog";
import {MessageService} from "../../services/message.service";
import {UserService} from "../../services/user.service";
import {SelectionModel} from "@angular/cdk/collections";
import {Action, ClubDialogBoxComponent} from "../../club-list/club-dialog-box/club-dialog-box.component";

@Component({
  selector: 'app-user-list',
  templateUrl: './user-list.component.html',
  styleUrls: ['./user-list.component.scss']
})
export class UserListComponent implements OnInit {

  basicTableData: BasicTableData<User> = new BasicTableData<User>();

  @ViewChild(MatPaginator, {static: true}) paginator!: MatPaginator;
  @ViewChild(MatTable, {static: true}) table: MatTable<any>;
  @ViewChild(MatSort, {static: true}) sort!: MatSort;

  constructor(private userService: UserService, public dialog: MatDialog, private messageService: MessageService) {
    this.basicTableData.columns = ['id', 'idCard', 'name', 'lastname', 'club'];
    this.basicTableData.columnsTags = ['idHeader', 'idCardHeader', 'nameHeader', 'lastnameHeader', 'clubHeader'];
    this.basicTableData.visibleColumns = ['name', 'lastname', 'club'];
    this.basicTableData.selection = new SelectionModel<User>(false, []);
    this.basicTableData.dataSource = new MatTableDataSource<User>();
  }

  ngOnInit(): void {
    this.showAllElements();
  }

  showAllElements(): void {
    this.userService.getAll().subscribe(users => {
      this.basicTableData.dataSource.data = users;
    });
  }

  addElement(): void {
    const user = new User();
    this.openDialog('Add a new club', Action.Add, user);
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

  setSelectedItem(row: User): void {
    if (row === this.basicTableData.selectedElement) {
      this.basicTableData.selectedElement = undefined;
    } else {
      this.basicTableData.selectedElement = row;
    }
  }

  openDialog(title: string, action: Action, user: User) {
    const dialogRef = this.dialog.open(ClubDialogBoxComponent, {
      width: '250px',
      data: {title: title, action: action, entity: user}
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

  addRowData(user: User) {
    this.userService.add(user).subscribe(user => {
      this.basicTableData.dataSource.data.push(user);
      this.basicTableData.dataSource._updateChangeSubscription();
      this.messageService.infoMessage("clubStored");
    });
  }

  updateRowData(user: User) {
    this.userService.update(user).subscribe(() => {
        this.messageService.infoMessage("clubUpdated");
      }
    );
  }

  deleteRowData(user: User) {
    this.userService.delete(user).subscribe(() => {
        this.basicTableData.dataSource.data = this.basicTableData.dataSource.data.filter(existing_user => existing_user !== user);
        this.messageService.infoMessage("clubDeleted");
      }
    );
  }

}
