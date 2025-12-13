import {AfterViewInit, Component} from '@angular/core';
import {MatDialog} from '@angular/material/dialog';
import {ClubService} from '../../services/club.service';
import {Club} from '../../models/club';
import {MessageService} from "../../services/message.service";
import {TRANSLOCO_SCOPE, TranslocoService} from "@ngneat/transloco";
import {RbacBasedComponent} from "../../components/RbacBasedComponent";
import {RbacService} from "../../services/rbac/rbac.service";
import {CompetitorsRankingComponent} from "../../components/competitors-ranking/competitors-ranking.component";
import {combineLatest} from "rxjs";
import {DatatableColumn} from "@biit-solutions/wizardry-theme/table";
import {DatePipe} from "@angular/common";
import {SystemOverloadService} from "../../services/notifications/system-overload.service";
import {ErrorHandler} from "@biit-solutions/wizardry-theme/utils";
import {BiitSnackbarService, NotificationType} from "@biit-solutions/wizardry-theme/info";
import {CustomDatePipe} from "../../pipes/visualization/custom-date-pipe";
import {Constants} from "../../constants";


@Component({
  selector: 'app-club-list',
  templateUrl: './club-list.component.html',
  styleUrls: ['./club-list.component.scss'],
  providers: [
    {
      provide: TRANSLOCO_SCOPE,
      multi: true,
      useValue: {scope: '', alias: 't'}
    }, CustomDatePipe, DatePipe
  ]
})
export class ClubListComponent extends RbacBasedComponent implements AfterViewInit {

  protected columns: DatatableColumn[] = [];
  protected pageSize: number = 10;
  protected pageSizes: number[] = [10, 25, 50, 100];

  protected loading: boolean = false;
  protected clubs: Club[];
  protected target: Club | null;
  protected confirm: boolean = false;

  constructor(private clubService: ClubService, public dialog: MatDialog, private messageService: MessageService,
              private transloco: TranslocoService, rbacService: RbacService, private _datePipe: DatePipe,
              private systemOverloadService: SystemOverloadService, private biitSnackbarService: BiitSnackbarService,) {
    super(rbacService);
  }

  datePipe() {
    return {
      transform: (value: any) => {
        !value ? value = 0 : value;
        return this._datePipe.transform(value, Constants.FORMAT.DATE);
      }
    }
  }

  ngAfterViewInit() {
    combineLatest(
      [
        this.transloco.selectTranslate('id'),
        this.transloco.selectTranslate('name'),
        this.transloco.selectTranslate('country'),
        this.transloco.selectTranslate('city'),
        this.transloco.selectTranslate('address'),
        this.transloco.selectTranslate('email'),
        this.transloco.selectTranslate('phone'),
        this.transloco.selectTranslate('web'),
        this.transloco.selectTranslate('createdBy'),
        this.transloco.selectTranslate('createdAt'),
        this.transloco.selectTranslate('updatedBy'),
        this.transloco.selectTranslate('updatedAt'),
      ]
    ).subscribe(([id, name, country, city, address, email, phone, web, createdBy, createdAt, updatedBy, updatedAt]) => {
      this.columns = [
        new DatatableColumn(id, 'id', false, 80),
        new DatatableColumn(name, 'name'),
        new DatatableColumn(country, 'country'),
        new DatatableColumn(city, 'city'),
        new DatatableColumn(address, 'address', false),
        new DatatableColumn(email, 'email', false),
        new DatatableColumn(phone, 'phone', false),
        new DatatableColumn(web, 'web', false),
        new DatatableColumn(createdBy, 'createdBy', false),
        new DatatableColumn(createdAt, 'createdAt', false, undefined, undefined, this.datePipe()),
        new DatatableColumn(updatedBy, 'updatedBy', false),
        new DatatableColumn(updatedAt, 'updatedAt', false, undefined, undefined, this.datePipe())
      ];
      this.loadData();
    });
  }

  loadData(): void {
    this.loading = true;
    this.systemOverloadService.isTransactionalBusy.next(true);
    this.clubService.getAll().subscribe({
      next: (_clubs: Club[]): void => {
        this.clubs = _clubs.map(_club => Club.clone(_club));
      },
      error: error => ErrorHandler.notify(error, this.transloco, this.biitSnackbarService)
    }).add(() => {
      this.loading = false;
      this.systemOverloadService.isTransactionalBusy.next(false);
    });
  }

  addElement(): void {
    const club: Club = new Club();
    this.target = club;
  }

  editElement(club: Club): void {
    this.target = club;
  }

  deleteElements(clubs: Club[]): void {
    if (clubs) {
      combineLatest(clubs.map(club => this.clubService.delete(club))).subscribe({
        next: (): void => {
          this.loadData();
          this.transloco.selectTranslate('infoClubDeleted').subscribe(
            translation => {
              this.biitSnackbarService.showNotification(translation, NotificationType.SUCCESS);
            }
          );
        },
        error: error => ErrorHandler.notify(error, this.transloco, this.biitSnackbarService)
      });
    }
  }

  showCompetitorsClassification(): void {
    if (this.target) {
      this.dialog.open(CompetitorsRankingComponent, {
        width: '85vw',
        data: {club: this.target, showIndex: true}
      });
    }
  }

  getClubNames(clubs: Club[]): string {
    if (clubs) {
      return clubs.map(club => club.name).join(', ');
    }
    return "";
  }

  onSaved(club: Club) {
    //Saved already on the popup.
    this.biitSnackbarService.showNotification(this.transloco.translate('infoClubStored'), NotificationType.INFO);
    this.loadData();
    this.target = null;
  }
}
