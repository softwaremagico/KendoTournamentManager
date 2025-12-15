import {AfterViewInit, Component} from '@angular/core';
import {Participant} from "../../models/participant";
import {MatDialog} from "@angular/material/dialog";
import {ParticipantService} from "../../services/participant.service";
import {ClubService} from "../../services/club.service";
import {Club} from "../../models/club";
import {TRANSLOCO_SCOPE, TranslocoService} from "@ngneat/transloco";
import {RbacService} from "../../services/rbac/rbac.service";
import {RbacBasedComponent} from "../../components/RbacBasedComponent";
import {Router} from "@angular/router";
import {UserSessionService} from "../../services/user-session.service";
import {CompetitorsRankingComponent} from "../../components/competitors-ranking/competitors-ranking.component";
import {CustomDatePipe} from "../../pipes/visualization/custom-date-pipe";
import {DatePipe} from "@angular/common";
import {DatatableColumn} from "@biit-solutions/wizardry-theme/table";
import {combineLatest} from "rxjs";
import {SystemOverloadService} from "../../services/notifications/system-overload.service";
import {ErrorHandler} from "@biit-solutions/wizardry-theme/utils";
import {BiitSnackbarService, NotificationType} from "@biit-solutions/wizardry-theme/info";
import {Constants} from "../../constants";
import {ClubNamePipe} from "../../pipes/visualization/club-name-pipe";

@Component({
  selector: 'app-participant-list',
  templateUrl: './participant-list.component.html',
  styleUrls: ['./participant-list.component.scss'],
  providers: [
    {
      provide: TRANSLOCO_SCOPE,
      multi: true,
      useValue: {scope: '', alias: 't'}
    }, CustomDatePipe, DatePipe, ClubNamePipe
  ]
})
export class ParticipantListComponent extends RbacBasedComponent implements AfterViewInit {

  protected columns: DatatableColumn[] = [];
  protected pageSize: number = 10;
  protected pageSizes: number[] = [10, 25, 50, 100];
  protected participants: Participant[];
  protected target: Participant | null;
  protected confirm: boolean = false;
  clubs: Club[];

  protected loading: boolean = false;
  protected showQr: boolean = false;
  protected showRanking: boolean = false;
  protected addPhoto: boolean = false;

  protected readonly port: number = +window.location.port;

  constructor(private router: Router, private userSessionService: UserSessionService,
              private participantService: ParticipantService, public dialog: MatDialog,
              private clubService: ClubService, private transloco: TranslocoService, rbacService: RbacService,
              private _datePipe: DatePipe, private _clubNamePipe: ClubNamePipe,
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
        this.transloco.selectTranslate('idCard'),
        this.transloco.selectTranslate('name'),
        this.transloco.selectTranslate('lastname'),
        this.transloco.selectTranslate('club'),
        this.transloco.selectTranslate('createdBy'),
        this.transloco.selectTranslate('createdAt'),
        this.transloco.selectTranslate('updatedBy'),
        this.transloco.selectTranslate('updatedAt'),
      ]
    ).subscribe(([id, idCard, name, lastname, clubName, createdBy, createdAt, updatedBy, updatedAt]) => {
      this.columns = [
        new DatatableColumn(id, 'id', false, 80),
        new DatatableColumn(idCard, 'idCard', false),
        new DatatableColumn(name, 'name'),
        new DatatableColumn(lastname, 'lastname'),
        new DatatableColumn(clubName, 'club', true, undefined, undefined, this._clubNamePipe),
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
    this.clubService.getAll().subscribe((_clubs: Club[]): void => {
      if (_clubs) {
        _clubs.sort(function (a: Club, b: Club) {
          return a.name.localeCompare(b.name);
        });
        this.clubs = _clubs
      }
    });
    this.participantService.getAll().subscribe({
      next: (_participants: Participant[]): void => {
        this.participants = _participants.map(_participant => Participant.clone(_participant));
      },
      error: error => ErrorHandler.notify(error, this.transloco, this.biitSnackbarService)
    }).add(() => {
      this.loading = false;
      this.systemOverloadService.isTransactionalBusy.next(false);
    });
  }

  addElement(): void {
    this.target = new Participant();
  }

  editElement(participant: Participant): void {
    this.target = participant;
  }

  deleteElements(participants: Participant[]): void {
    if (participants) {
      combineLatest(participants.map(participant => this.participantService.delete(participant))).subscribe({
        next: (): void => {
          this.loadData();
          this.transloco.selectTranslate('infoParticipantDeleted').subscribe(
            translation => {
              this.biitSnackbarService.showNotification(translation, NotificationType.SUCCESS);
            }
          );
        },
        error: error => ErrorHandler.notify(error, this.transloco, this.biitSnackbarService)
      });
    }
  }

  openStatistics(participant: Participant): void {
    if (participant) {
      this.userSessionService.setSelectedParticipant(participant.id + "");
      this.router.navigate(['/participants/statistics'], {state: {participantId: participant.id}});
    }
  }

  showCompetitorsClassification(participant: Participant): void {
    this.dialog.open(CompetitorsRankingComponent, {
      panelClass: 'pop-up-panel',
      width: '85vw',
      data: {competitor: participant, showIndex: true}
    });
  }

  onSaved($event: Participant) {
    this.biitSnackbarService.showNotification(this.transloco.translate('infoParticipantStored'), NotificationType.INFO);
    this.loadData();
    this.target = null;
  }

  getParticipantNames(participants: Participant[]): string {
    if (participants) {
      return participants.map(participant => (participant.name + " " + participant.lastname)).join(', ');
    }
    return "";
  }
}
