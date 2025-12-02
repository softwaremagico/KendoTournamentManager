import {Component, Input, OnDestroy, OnInit, ViewChild} from '@angular/core';
import {BasicTableData} from "./basic-table-data";
import {MatDialog} from "@angular/material/dialog";
import {MatPaginator, PageEvent} from '@angular/material/paginator';
import {MatSort} from "@angular/material/sort";
import {UserSessionService} from "../../../services/user-session.service";
import {TranslocoService} from "@ngneat/transloco";
import {DatePipe} from "@angular/common";
import {Subscription} from "rxjs";
import {Message} from "@stomp/stompjs";
import {MessageContent} from "../../../websockets/message-content.model";
import {RxStompService} from "../../../websockets/rx-stomp.service";
import {EnvironmentService} from "../../../environment.service";

@Component({
  selector: 'basic-table',
  templateUrl: './basic-table.component.html',
  styleUrls: ['./basic-table.component.scss']
})
export class BasicTableComponent implements OnInit, OnDestroy {

  private websocketsPrefix: string = this.environmentService.getWebsocketPrefix();

  @ViewChild(MatPaginator) paginator: MatPaginator;
  @ViewChild(MatSort) sort: MatSort;

  @Input()
  basicTableData: BasicTableData<any>;

  @Input()
  disableRow: (argument: () => any) => boolean;

  pipe: DatePipe;

  private topicSubscription: Subscription;

  constructor(public dialog: MatDialog, private translateService: TranslocoService,
              private userSessionService: UserSessionService, private environmentService: EnvironmentService,
              private rxStompService: RxStompService) {
    this.setLocale();
    this.connectToWebsockets();
  }

  ngOnInit(): void {
    this.basicTableData.dataSource.filterPredicate = (data: any, filter: string): boolean => {
      filter = filter.toLowerCase().normalize('NFD').replace(/\p{Diacritic}/gu, "");
      const dataSearch: string = Object.keys(data).reduce((searchTerm: string, key: string) => {
        return (searchTerm + (data as { [key: string]: any })[key]);
      }, '').normalize('NFD').replace(/\p{Diacritic}/gu, "").toLowerCase();

      const transformedFilter: string = filter.trim().normalize('NFD').replace(/\p{Diacritic}/gu, "").toLowerCase();

      return dataSearch.indexOf(transformedFilter) != -1;
    }
  }

  ngOnDestroy(): void {
    this.topicSubscription?.unsubscribe();
  }

  connectToWebsockets(): void {
    this.topicSubscription = this.rxStompService.watch(this.websocketsPrefix + '/creates').subscribe((message: Message): void => {
      const messageContent: MessageContent = JSON.parse(message.body);
      if (messageContent.type && messageContent.type.toLowerCase() == "created" && (!messageContent.session || messageContent.session !== localStorage.getItem('session'))) {
        if (this.basicTableData.element === messageContent.topic || this.basicTableData.element + "DTO" === messageContent.topic) {
          const element = JSON.parse(messageContent.payload);
          if (this.basicTableData.dataSource.data.findIndex(obj => obj.id === element.id) < 0) {
            this.basicTableData.dataSource.data.push(element);
            this.basicTableData.dataSource._updateChangeSubscription();
          }
        }
      }
    });

    this.topicSubscription = this.rxStompService.watch(this.websocketsPrefix + '/updates').subscribe((message: Message): void => {
      const messageContent: MessageContent = JSON.parse(message.body);
      if (messageContent.type && messageContent.type.toLowerCase() == "updated" && (!messageContent.session || messageContent.session !== localStorage.getItem('session'))) {
        if (this.basicTableData.element === messageContent.topic || this.basicTableData.element + "DTO" === messageContent.topic) {
          const element = JSON.parse(messageContent.payload);
          let index: number = this.basicTableData.dataSource.data.findIndex(obj => obj.id === element.id);
          if (index >= 0) {
            this.basicTableData.dataSource.data[index] = element;
            this.basicTableData.dataSource._updateChangeSubscription();
            //If it is selected, keep it selected.
            if (this.basicTableData.selectedElement?.id === element.id) {
              this.basicTableData.selectedElement = element;
              this.basicTableData.selectItem(element);
            }
          }
        }
      }
    });

    this.topicSubscription = this.rxStompService.watch(this.websocketsPrefix + '/deletes').subscribe((message: Message): void => {
      const messageContent: MessageContent = JSON.parse(message.body);
      if (messageContent.type && messageContent.type.toLowerCase() == "deleted" && (!messageContent.session || messageContent.session !== localStorage.getItem('session'))) {
        if (this.basicTableData.element === messageContent.topic || this.basicTableData.element + "DTO" === messageContent.topic) {
          const element = JSON.parse(messageContent.payload);
          this.basicTableData.dataSource.data = this.basicTableData.dataSource.data.filter(obj => obj.id !== element.id);
          this.basicTableData.dataSource._updateChangeSubscription();
          if (this.basicTableData.selectedElement?.id === element.id) {
            this.basicTableData.selectedElement = undefined;
          }
        }
      }
    });
  }

  ngAfterViewInit(): void {
    this.basicTableData.dataSource.paginator = this.paginator;
    this.basicTableData.dataSource.sort = this.sort;
  }

  private setLocale(): void {
    if (this.userSessionService.getLanguage() === 'es' || this.userSessionService.getLanguage() === 'ca') {
      this.pipe = new DatePipe('es');
    } else if (this.userSessionService.getLanguage() === 'it') {
      this.pipe = new DatePipe('it');
    } else if (this.userSessionService.getLanguage() === 'de') {
      this.pipe = new DatePipe('de');
    } else if (this.userSessionService.getLanguage() === 'nl') {
      this.pipe = new DatePipe('nl');
    } else {
      this.pipe = new DatePipe('en-US');
    }
  }

  setSelectedItem(row: any): void {
    if (row === this.basicTableData.selectedElement) {
      this.basicTableData.selectedElement = undefined;
    } else {
      this.basicTableData.selectedElement = row;
    }
  }

  filter(filter: string): void {
    this.basicTableData.dataSource.filter = filter;
  }

  isColumnVisible(column: string): boolean {
    return this.basicTableData.visibleColumns.includes(column);
  }

  toggleColumnVisibility(column: string): void {
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

  getDefaultPageSize(): number {
    return this.userSessionService.getItemsPerPage();
  }


  onPaginateChange($event: PageEvent): void {
    this.userSessionService.setItemsPerPage($event.pageSize);
  }

  getColumnData(column: any): any {
    if (typeof column === 'number') {
      return column;
    } else if (typeof column === 'boolean') {
      return column ? this.translateService.translate('yes') : this.translateService.translate('no');
      //Is it a date?
    } else if (isNaN(column) && !isNaN(Date.parse(column)) && (column instanceof Date)) {
      return this.pipe.transform(column, 'short');
    } else if (column instanceof Object) {
      return this.translateService.translate(column.toString());
    } else {
      if (column) {
        const text: string = (column as string);
        if (text.toUpperCase() === text) {
          //probably is an enum
          return this.translateService.translate(this.snakeToCamel(text.toLowerCase()));
        } else {
          return this.translateService.translate(text);
        }
      } else {
        return "";
      }
    }
  }

  snakeToCamel(string: string): string {
    return string.toLowerCase().replace(/[-_][a-z]/g, (group: string) => group.slice(-1).toUpperCase());
  }
}
