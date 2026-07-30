import {AfterViewInit, ChangeDetectorRef, Component} from '@angular/core';
import {Participant} from "../../models/participant";
import {ParticipantService} from "../../services/participant.service";
import {ClubService} from "../../services/club.service";
import {Club} from "../../models/club";
import {TRANSLOCO_SCOPE, TranslocoService} from '@jsverse/transloco';
import {RbacService} from "../../services/rbac/rbac.service";
import {RbacBasedComponent} from "../../components/RbacBasedComponent";
import {Router} from "@angular/router";
import {UserSessionService} from "../../services/user-session.service";
import {CustomDatePipe} from "../../pipes/visualization/custom-date-pipe";
import {DatePipe} from "@angular/common";
import {DatatableColumn} from "@biit-solutions/wizardry-theme/table";
import {combineLatest, forkJoin, takeUntil} from "rxjs";
import {SystemOverloadService} from "../../services/notifications/system-overload.service";
import {ErrorHandler} from "@biit-solutions/wizardry-theme/utils";
import {BiitSnackbarService, NotificationType} from "@biit-solutions/wizardry-theme/info";
import {Constants} from "../../constants";
import {ClubNamePipe} from "../../pipes/visualization/club-name-pipe";

@Component({
  standalone: false,
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
  private static readonly COLUMN_TRANSLATION_KEYS: readonly string[] = [
    'id',
    'idCard',
    'name',
    'lastname',
    'club',
    'createdBy',
    'createdAt',
    'updatedBy',
    'updatedAt'
  ];

  protected columns: DatatableColumn[] = [];
  protected pageSize: number = 10;
  protected pageSizes: number[] = [10, 25, 50, 100];
  protected participants: Participant[] = [];
  protected target: Participant | null = null;
  protected confirmDelete: boolean = false;
  clubs: Club[] = [];

  protected loading: boolean = false;
  protected showQr: boolean = false;
  protected showRanking: boolean = false;
  protected addPhoto: boolean = false;

  protected readonly port: number = +globalThis.location.port;

   constructor(private readonly router: Router, private readonly userSessionService: UserSessionService,
               private readonly participantService: ParticipantService,
               private readonly clubService: ClubService, private readonly transloco: TranslocoService, rbacService: RbacService,
               private readonly _datePipe: DatePipe, private readonly _clubNamePipe: ClubNamePipe,
               private readonly systemOverloadService: SystemOverloadService, private readonly biitSnackbarService: BiitSnackbarService,
               private readonly cdr: ChangeDetectorRef) {
     super(rbacService);
   }

  datePipe(): { transform: (value: number | string | Date | null | undefined) => string | null } {
    return {
      transform: (value: number | string | Date | null | undefined = 0) => this._datePipe.transform(value, Constants.FORMAT.DATE)
    }
  }

  private buildColumns(labels: string[]): DatatableColumn[] {
    const [id, idCard, name, lastname, clubName, createdBy, createdAt, updatedBy, updatedAt] = labels;
    return [
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
  }

  private sortClubs(clubs: Club[]): Club[] {
    return clubs.sort((a: Club, b: Club): number => a.name.localeCompare(b.name));
  }

  ngAfterViewInit() {
    combineLatest(ParticipantListComponent.COLUMN_TRANSLATION_KEYS.map((key: string) => this.transloco.selectTranslate(key)))
      .pipe(takeUntil(this.destroySubject)).subscribe((labels: string[]) => {
      this.columns = this.buildColumns(labels);
      this.loadData();
    });
  }

   loadData(): void {
     this.loading = true;
     this.systemOverloadService.isTransactionalBusy.next(true);
     forkJoin({
       clubs: this.clubService.getAll(),
       participants: this.participantService.getAll()
     }).pipe(takeUntil(this.destroySubject)).subscribe({
       next: ({clubs, participants}: { clubs: Club[], participants: Participant[] }): void => {
         this.clubs = clubs ? this.sortClubs(clubs) : [];
         this.participants = participants.map(_participant => Participant.clone(_participant));
         this.cdr.markForCheck();
       },
       error: error => ErrorHandler.notify(error, this.transloco as never, this.biitSnackbarService)
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
      combineLatest(participants.map(participant => this.participantService.delete(participant))).pipe(takeUntil(this.destroySubject)).subscribe({
        next: (): void => {
          this.loadData();
          this.biitSnackbarService.showNotification(this.transloco.translate('infoParticipantDeleted'), NotificationType.SUCCESS);
          this.confirmDelete = false;
        },
        error: error => ErrorHandler.notify(error, this.transloco as never, this.biitSnackbarService)
      });
    }
  }

  openStatistics(participant: Participant): void {
    if (participant) {
      this.userSessionService.setSelectedParticipant(participant.id + "");
      this.router.navigate(['/participants/statistics'], {state: {participantId: participant.id}});
    }
  }

  onSaved(_savedParticipant?: Participant): void {
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
