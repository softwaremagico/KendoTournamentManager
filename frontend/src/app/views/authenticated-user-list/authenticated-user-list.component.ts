import {Component, OnInit, ViewChild} from '@angular/core';
import {BasicTableData} from "../../components/basic/basic-table/basic-table-data";
import {MatPaginator} from "@angular/material/paginator";
import {MatTable, MatTableDataSource} from "@angular/material/table";
import {MatSort} from "@angular/material/sort";
import {MatDialog} from "@angular/material/dialog";
import {MessageService} from "../../services/message.service";
import {TranslateService} from "@ngx-translate/core";
import {SelectionModel} from "@angular/cdk/collections";
import {Action} from "../../action";
import {AuthenticatedUser} from "../../models/authenticated-user";
import {LoginService} from "../../services/login.service";
import {
  AuthenticatedUserDialogBoxComponent
} from "./authenticated-user-dialog-box/authenticated-user-dialog-box.component";
import {UserService} from "../../services/user.service";
import {UserRoles} from "../../services/rbac/user-roles";
import {RbacBasedComponent} from "../../components/RbacBasedComponent";
import {RbacService} from "../../services/rbac/rbac.service";

@Component({
  selector: 'app-authenticated-user-list',
  templateUrl: './authenticated-user-list.component.html',
  styleUrls: ['./authenticated-user-list.component.scss']
})
export class AuthenticatedUserListComponent extends RbacBasedComponent implements OnInit {

  basicTableData: BasicTableData<AuthenticatedUser> = new BasicTableData<AuthenticatedUser>("AuthenticatedUser");

  @ViewChild(MatPaginator, {static: true}) paginator!: MatPaginator;
  @ViewChild(MatTable, {static: true}) table: MatTable<any>;
  @ViewChild(MatSort, {static: true}) sort!: MatSort;

  constructor(private loginService: LoginService, private userService: UserService, public dialog: MatDialog, private messageService: MessageService,
              private translateService: TranslateService, rbacService: RbacService) {
    super(rbacService);
    this.basicTableData.columns = ['id', 'username', 'name', 'lastname', 'roles'];
    this.basicTableData.columnsTags = ['id', 'username', 'name', 'lastname', 'roles'];
    this.basicTableData.visibleColumns = ['username', 'name', 'lastname'];
    this.basicTableData.selection = new SelectionModel<AuthenticatedUser>(false, []);
    this.basicTableData.dataSource = new MatTableDataSource<AuthenticatedUser>();
  }

  ngOnInit(): void {
    this.showAllElements();
  }

  showAllElements(): void {
    this.userService.getAll().subscribe(authenticatedUsers => {
      this.basicTableData.dataSource.data = authenticatedUsers;
    });
  }

  addElement(): void {
    const authenticatedUser: AuthenticatedUser = new AuthenticatedUser();
    authenticatedUser.roles[0] = UserRoles.VIEWER;
    this.openDialog(this.translateService.instant('authenticatedUserAdd'), Action.Add, new AuthenticatedUser());
  }

  editElement(): void {
    if (this.basicTableData.selectedElement) {
      this.openDialog(this.translateService.instant('authenticatedUserEdit'), Action.Update, this.basicTableData.selectedElement);
    }
  }

  deleteElement(): void {
    if (this.basicTableData.selectedElement) {
      this.openDialog(this.translateService.instant('authenticatedUserDelete'), Action.Delete, this.basicTableData.selectedElement);
    }
  }

  setSelectedItem(row: AuthenticatedUser): void {
    if (row === this.basicTableData.selectedElement) {
      this.basicTableData.selectedElement = undefined;
    } else {
      this.basicTableData.selectedElement = row;
    }
  }

  openDialog(title: string, action: Action, authenticatedUser: AuthenticatedUser) {
    const dialogRef = this.dialog.open(AuthenticatedUserDialogBoxComponent, {
      width: '400px',
      data: {title: title, action: action, entity: authenticatedUser}
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result == undefined) {
        //Do nothing
      } else if (result == Action.Cancel) {
        //Do nothing
      } else if (result.action == Action.Add) {
        this.addRowData(result.data);
      } else if (result.action == Action.Update) {
        this.updateRowData(result.data);
      } else if (result.action == Action.Delete) {
        this.deleteRowData(result.data);
      }
    });
  }

  addRowData(authenticatedUser: AuthenticatedUser): void {
    this.userService.add(authenticatedUser).subscribe((_authenticatedUser: AuthenticatedUser): void => {
      //If data is not already added though table webservice.
      if (this.basicTableData.dataSource.data.findIndex((obj: AuthenticatedUser): boolean => obj.id === _authenticatedUser.id) < 0) {
        this.basicTableData.dataSource.data.push(_authenticatedUser);
        this.basicTableData.dataSource._updateChangeSubscription();
      }
      this.basicTableData.selectItem(_authenticatedUser);
    });
  }

  updateRowData(authenticatedUser: AuthenticatedUser) {
    this.userService.update(authenticatedUser).subscribe(() => {
        this.messageService.infoMessage('infoAuthenticatedUserUpdated');
      }
    );
  }

  deleteRowData(authenticatedUser: AuthenticatedUser) {
    this.userService.delete(authenticatedUser).subscribe(() => {
        this.basicTableData.dataSource.data = this.basicTableData.dataSource.data.filter(existing_authenticatedUser => existing_authenticatedUser !== authenticatedUser);
        this.messageService.infoMessage('infoAuthenticatedUserDeleted');
        this.basicTableData.selectedElement = undefined;
      }
    );
  }

  disableRow(argument: any): boolean {
    return false;
  }


}
