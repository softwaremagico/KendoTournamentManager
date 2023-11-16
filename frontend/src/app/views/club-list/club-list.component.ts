import {Component, OnInit, ViewChild} from '@angular/core';
import {MatPaginator} from '@angular/material/paginator';
import {MatTable, MatTableDataSource} from '@angular/material/table';
import {MatSort} from '@angular/material/sort';
import {MatDialog} from '@angular/material/dialog';
import {SelectionModel} from "@angular/cdk/collections";
import {ClubService} from '../../services/club.service';
import {Club} from '../../models/club';
import {ClubDialogBoxComponent} from './club-dialog-box/club-dialog-box.component';
import {MessageService} from "../../services/message.service";
import {BasicTableData} from "../../components/basic/basic-table/basic-table-data";
import {Action} from "../../action";
import {TranslateService} from "@ngx-translate/core";
import {RbacBasedComponent} from "../../components/RbacBasedComponent";
import {RbacService} from "../../services/rbac/rbac.service";
import {CompetitorsRankingComponent} from "../../components/competitors-ranking/competitors-ranking.component";


@Component({
  selector: 'app-club-list',
  templateUrl: './club-list.component.html',
  styleUrls: ['./club-list.component.scss']
})
export class ClubListComponent extends RbacBasedComponent implements OnInit {

  basicTableData: BasicTableData<Club> = new BasicTableData<Club>();

  @ViewChild(MatPaginator, {static: true}) paginator!: MatPaginator;
  @ViewChild(MatTable, {static: true}) table: MatTable<any>;
  @ViewChild(MatSort, {static: true}) sort!: MatSort;

  constructor(private clubService: ClubService, public dialog: MatDialog, private messageService: MessageService,
              private translateService: TranslateService, rbacService: RbacService) {
    super(rbacService);
    this.basicTableData.columns = ['id', 'name', 'country', 'city', 'address', 'email', 'phone', 'web',
      'createdAt', 'createdBy', 'updatedAt', 'updatedBy'];
    this.basicTableData.columnsTags = ['id', 'name', 'country', 'city', 'address', 'email', 'phone', 'web',
      'createdAt', 'createdBy', 'updatedAt', 'updatedBy'];
    this.basicTableData.visibleColumns = ['name', 'country', 'city'];
    this.basicTableData.selection = new SelectionModel<Club>(false, []);
    this.basicTableData.dataSource = new MatTableDataSource<Club>();
  }

  ngOnInit(): void {
    this.showAllElements();
  }

  showAllElements(): void {
    this.clubService.getAll().subscribe((clubs: Club[]): void => {
      this.basicTableData.dataSource.data = clubs;
    });
  }

  addElement(): void {
    this.openDialog(this.translateService.instant('clubAdd'), Action.Add, new Club());
  }

  editElement(): void {
    if (this.basicTableData.selectedElement) {
      this.openDialog(this.translateService.instant('clubEdit'), Action.Update, this.basicTableData.selectedElement);
    }
  }

  deleteElement(): void {
    if (this.basicTableData.selectedElement) {
      this.openDialog(this.translateService.instant('clubDelete'), Action.Delete, this.basicTableData.selectedElement);
    }
  }

  setSelectedItem(row: Club): void {
    if (row === this.basicTableData.selectedElement) {
      this.basicTableData.selectedElement = undefined;
    } else {
      this.basicTableData.selectedElement = row;
    }
  }

  openDialog(title: string, action: Action, club: Club): void {
    const dialogRef = this.dialog.open(ClubDialogBoxComponent, {
      width: '400px',
      data: {title: title, action: action, entity: club}
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result == undefined) {
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

  addRowData(club: Club) {
    this.clubService.add(club).subscribe((_club: Club): void => {
      this.basicTableData.dataSource.data.push(_club);
      this.basicTableData.dataSource._updateChangeSubscription();
      this.basicTableData.selectItem(_club);
      this.messageService.infoMessage('infoClubStored');
    });
  }

  updateRowData(club: Club) {
    this.clubService.update(club).subscribe((): void => {
        this.messageService.infoMessage('infoClubUpdated');
      }
    );
  }

  deleteRowData(club: Club): void {
    this.clubService.delete(club).subscribe(() => {
        this.basicTableData.dataSource.data = this.basicTableData.dataSource.data.filter((existing_club: Club): boolean => existing_club !== club);
        this.messageService.infoMessage('infoClubDeleted');
        this.basicTableData.selectedElement = undefined;
      }
    );
  }

  disableRow(argument: any): boolean {
    return false;
  }

  showCompetitorsClassification(): void {
    if (this.basicTableData.selectedElement) {
      this.dialog.open(CompetitorsRankingComponent, {
        width: '85vw',
        data: {club: this.basicTableData.selectedElement, showIndex: true}
      });
    }
  }
}
