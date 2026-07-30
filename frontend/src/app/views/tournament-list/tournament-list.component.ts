import {AfterViewInit, ChangeDetectorRef, Component, QueryList, TemplateRef, ViewChild, ViewChildren} from '@angular/core';
import {Tournament} from "../../models/tournament";
import {TournamentService} from "../../services/tournament.service";
import {MessageService} from "../../services/message.service";

import {Router} from '@angular/router';
import {UserSessionService} from "../../services/user-session.service";
import {RankingService} from "../../services/ranking.service";
import {TRANSLOCO_SCOPE, TranslocoService} from '@jsverse/transloco';
import {RbacService} from "../../services/rbac/rbac.service";
import {RbacBasedComponent} from "../../components/RbacBasedComponent";
import {SystemOverloadService} from "../../services/notifications/system-overload.service";
import {AchievementsService} from "../../services/achievements.service";
import {BiitDatatableComponent, DatatableColumn} from "@biit-solutions/wizardry-theme/table";
import {combineLatest, takeUntil} from "rxjs";
import {DatePipe} from "@angular/common";
import {ErrorHandler} from "@biit-solutions/wizardry-theme/utils";
import {BiitProgressBarType, BiitSnackbarService, NotificationType} from "@biit-solutions/wizardry-theme/info";
import {TableColumnTranslationPipe} from "../../pipes/visualization/table-column-translation-pipe";
import {CustomDatePipe} from "../../pipes/visualization/custom-date-pipe";
import {Constants} from "../../constants";
import {RoleType} from "../../models/role-type";

@Component({
  standalone: false,
  selector: 'app-tournament-list',
  templateUrl: './tournament-list.component.html',
  styleUrls: ['./tournament-list.component.scss'],
  providers: [
    {
      provide: TRANSLOCO_SCOPE,
      multi: true,
      useValue: {scope: '', alias: 't'}
    }, TableColumnTranslationPipe, CustomDatePipe, DatePipe
  ]
})
export class TournamentListComponent extends RbacBasedComponent implements AfterViewInit {
  private static readonly COLUMN_TRANSLATION_KEYS: readonly string[] = [
    'id',
    'name',
    'tournamentType',
    'scoreRules',
    'locked',
    'shiaijos',
    'teamSize',
    'createdBy',
    'createdAt',
    'updatedBy',
    'updatedAt'
  ];

  protected columns: DatatableColumn[] = [];
  protected pageSize: number = 10;
  protected pageSizes: number[] = [10, 25, 50, 100];
  protected tournaments: Tournament[] = [];
  protected target: Tournament | null = null;
  protected confirmDelete: boolean = false;
  protected confirmClone: boolean = false;
  protected showTournamentRoles: boolean = false;
  protected showTournamentTeams: boolean = false;
  protected showDiplomasRoles: boolean = false;
  protected showAccreditationRoles: boolean = false;

  protected loading: boolean = false;
  protected loadingGlobal: boolean = false;
  protected showQr: boolean = false;
  protected readonly port: number = +window.location.port;

  @ViewChildren('booleanCell') booleanCellTemplates: QueryList<TemplateRef<unknown>>;
  @ViewChild('table')
  table: BiitDatatableComponent<Tournament>;

  public lockedTournaments: (row: Tournament) => boolean = (row) => row.locked;

   constructor(private readonly router: Router, private readonly userSessionService: UserSessionService, private readonly tournamentService: TournamentService,
               private readonly rankingService: RankingService,
               private readonly messageService: MessageService, rbacService: RbacService, private readonly systemOverloadService: SystemOverloadService,
               private readonly achievementsService: AchievementsService, private readonly transloco: TranslocoService, private readonly _datePipe: DatePipe,
               private readonly biitSnackbarService: BiitSnackbarService, private readonly tableColumnTranslationPipe: TableColumnTranslationPipe,
               private readonly cdr: ChangeDetectorRef) {
     super(rbacService);
   }

  datePipe(): { transform: (value?: number | string | Date | null) => string | null } {
    return {
      transform: (value: number | string | Date | null = 0) => this._datePipe.transform(value, Constants.FORMAT.DATE)
    }
  }

  private buildColumns(labels: string[]): DatatableColumn[] {
    const [id, name, type, scoreRules, locked, shiaijos, teamSize, createdBy, createdAt, updatedBy, updatedAt] = labels;
    return [
      new DatatableColumn(id, 'id', false, 80),
      new DatatableColumn(name, 'name'),
      new DatatableColumn(type, 'type', true, undefined, undefined, this.tableColumnTranslationPipe),
      new DatatableColumn(scoreRules, 'tournamentScore', false, undefined, undefined, this.tableColumnTranslationPipe),
      new DatatableColumn(locked, 'locked', false, 200, undefined, undefined, this.booleanCellTemplates.first),
      new DatatableColumn(shiaijos, 'shiaijos', false, 150),
      new DatatableColumn(teamSize, 'teamSize', true, 150),
      new DatatableColumn(createdBy, 'createdBy', false),
      new DatatableColumn(createdAt, 'createdAt', false, undefined, undefined, this.datePipe()),
      new DatatableColumn(updatedBy, 'updatedBy', false),
      new DatatableColumn(updatedAt, 'updatedAt', false, undefined, undefined, this.datePipe())
    ];
  }

  private downloadFile(content: BlobPart, type: string, fileName: string): void {
    const blob: Blob = new Blob([content], {type});
    const downloadURL: string = window.URL.createObjectURL(blob);
    const anchor: HTMLAnchorElement = document.createElement('a');
    anchor.download = fileName;
    anchor.href = downloadURL;
    anchor.click();
    window.URL.revokeObjectURL(downloadURL);
  }

  ngAfterViewInit() {
    combineLatest(TournamentListComponent.COLUMN_TRANSLATION_KEYS.map((key: string) => this.transloco.selectTranslate(key)))
      .pipe(takeUntil(this.destroySubject)).subscribe((labels: string[]) => {
      this.columns = this.buildColumns(labels);
      this.loadData();
    });
  }

   loadData(tournament?: Tournament): void {
     this.loading = true;
     this.systemOverloadService.isTransactionalBusy.next(true);
     this.tournamentService.getAll().pipe(takeUntil(this.destroySubject)).subscribe({
       next: (_tournaments: Tournament[]): void => {
         this.tournaments = _tournaments.map(_tournament => Tournament.clone(_tournament)).sort((a: Tournament, b: Tournament): number => {
           return new Date(b.createdAt).getTime() - new Date(a.createdAt).getTime();
         });
         this.cdr.markForCheck();
       },
       error: error => ErrorHandler.notify(error, this.transloco as never, this.biitSnackbarService)
     }).add(() => {
       this.loading = false;
       this.systemOverloadService.isTransactionalBusy.next(false);
       this.selectItem(tournament);
     });
   }

  addElement(): void {
    const tournament: Tournament = new Tournament();
    tournament.duelsDuration = Tournament.DEFAULT_DUELS_DURATION;
    tournament.type = Tournament.DEFAULT_TYPE;
    tournament.shiaijos = Tournament.DEFAULT_SHIAIJOS;
    tournament.teamSize = Tournament.DEFAULT_TEAM_SIZE;
    this.target = tournament;
  }

  editElement(tournament: Tournament): void {
    this.target = tournament;
    this.userSessionService.setSelectedTournament(tournament.id + "");
  }

  deleteElements(tournaments: Tournament[]): void {
    if (tournaments) {
      combineLatest(tournaments.map(tournament => this.tournamentService.delete(tournament))).pipe(takeUntil(this.destroySubject)).subscribe({
        next: (): void => {
          this.loadData();
          this.confirmDelete = false;
          this.biitSnackbarService.showNotification(this.transloco.translate('infoTournamentDeleted'), NotificationType.SUCCESS);
        },
          error: error => ErrorHandler.notify(error, this.transloco as never, this.biitSnackbarService)
      });
    }
  }

  openFights(tournament: Tournament): void {
    if (tournament) {
      this.userSessionService.setSelectedTournament(tournament.id + "");
      this.router.navigate(['/tournaments/fights'], {state: {tournamentId: tournament.id}});
    }
  }

  downloadBlogCode(tournament: Tournament): void {
    if (tournament?.id) {
      this.loadingGlobal = true;
      this.rankingService.getTournamentSummaryAsHtml(tournament.id).subscribe((html: Blob): void => {
        this.downloadFile(html, 'txt/plain', "Code - " + tournament.name + '.txt');
      }).add(() => {
        this.loadingGlobal = false;
      });
    }
  }

  downloadAccreditations(data: { tournament: Tournament, roles: RoleType[], newOnes: boolean }): void {
    if (data?.tournament?.id) {
      this.tournamentService.getAccreditations(data.tournament.id, data.newOnes, data.roles).subscribe((html: Blob): void => {
        if (html !== null) {
          this.downloadFile(html, 'application/pdf', 'Accreditations - ' + data.tournament.name + '.pdf');
          this.showAccreditationRoles = false;
        } else {
          this.messageService.warningMessage('noResults');
        }
      });
    } else {
      this.showAccreditationRoles = false;
    }
  }

  downloadDiplomas(data: { tournament: Tournament, roles: RoleType[], newOnes: boolean }): void {
    if (data?.tournament?.id) {
      this.loadingGlobal = true;
      this.tournamentService.getDiplomas(data.tournament.id, data.newOnes, data.roles).subscribe((html: Blob) => {
        if (html !== null) {
          this.downloadFile(html, 'application/pdf', 'Diplomas - ' + data.tournament.name + '.pdf');
          this.showDiplomasRoles = false;
        } else {
          this.messageService.warningMessage('noResults');
        }
      }).add(() => {
        this.loadingGlobal = false;
      });
    } else {
      this.showDiplomasRoles = false;
    }
  }

  lockElement(tournament: Tournament, locked: boolean): void {
    if (tournament) {
      tournament.locked = locked;
      if (locked) {
        this.achievementsService.regenerateTournamentAchievements(tournament?.id!).subscribe();
        tournament.lockedAt ??= new Date();
        tournament.finishedAt ??= new Date();
      }
      this.tournamentService.update(tournament).subscribe((_tournament: Tournament): void => {
          this.loadData();
          this.target = null;
          this.messageService.infoMessage('infoTournamentUpdated');
        }
      );
    }
  }

  openStatistics(tournament: Tournament): void {
    if (tournament) {
      this.userSessionService.setSelectedTournament(tournament.id + "");
      this.router.navigate(['/tournaments/statistics'], {state: {tournamentId: tournament.id}});
    }
  }

  cloneElement(tournament: Tournament): void {
    const tournamentId: number = tournament?.id!;
    this.tournamentService.clone(tournamentId).subscribe((_tournament: Tournament): void => {
      this.loadData();
      this.messageService.infoMessage('infoTournamentStored');
    }).add(() => {
      this.confirmClone = false;
    })
  }

  downloadZip(tournament: Tournament): void {
    if (tournament?.id) {
      this.loadingGlobal = true;
      this.rankingService.getAllListAsZip(tournament.id).subscribe((html: Blob): void => {
        this.downloadFile(html, 'application/zip', tournament.name + '.zip');
      }).add(() => {
        this.loadingGlobal = false
      });
    }
  }

  onSaved(tournament: Tournament) {
    //Saved already on the popup.
    this.biitSnackbarService.showNotification(this.transloco.translate('infoTournamentStored'), NotificationType.INFO);
    this.loadData(tournament);
    this.target = null;
  }

  selectItem(tournament?: Tournament) {
    if (tournament) {
      // const selectedItems: Tournament[] = [];
      // selectedItems.push(tournament);
      // this.table.selectedRows = selectedItems;
    }
  }

  getTournamentNames(tournaments: Tournament[]): string {
    if (tournaments) {
      return tournaments.map(tournament => tournament.name).join(', ');
    }
    return "";
  }

  protected readonly BiitProgressBarType = BiitProgressBarType;

  selectTournaments(tournaments: Tournament[]) {
    if (tournaments?.length === 1) {
      this.userSessionService.setSelectedTournament(tournaments[0].id + "");
    } else {
      this.userSessionService.setSelectedTournament(undefined);
    }
  }
}
