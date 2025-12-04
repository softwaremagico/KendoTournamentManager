import {AfterViewInit, Component, OnInit, QueryList, TemplateRef, ViewChildren} from '@angular/core';
import {Tournament} from "../../models/tournament";
import {TournamentService} from "../../services/tournament.service";
import {MatDialog, MatDialogRef} from "@angular/material/dialog";
import {MessageService} from "../../services/message.service";
import {TournamentDialogBoxComponent} from "./tournament-dialog-box/tournament-dialog-box.component";
import {TournamentRolesComponent} from "./tournament-roles/tournament-roles.component";
import {TournamentTeamsComponent} from "./tournament-teams/tournament-teams.component";

import {Router} from '@angular/router';
import {UserSessionService} from "../../services/user-session.service";
import {Action} from "../../action";
import {RankingService} from "../../services/ranking.service";
import {TRANSLOCO_SCOPE, TranslocoService} from "@ngneat/transloco";
import {RbacService} from "../../services/rbac/rbac.service";
import {RbacBasedComponent} from "../../components/RbacBasedComponent";
import {
  RoleSelectorDialogBoxComponent
} from "../../components/role-selector-dialog-box/role-selector-dialog-box.component";
import {SystemOverloadService} from "../../services/notifications/system-overload.service";
import {AchievementsService} from "../../services/achievements.service";
import {ConfirmationDialogComponent} from "../../components/basic/confirmation-dialog/confirmation-dialog.component";
import {TournamentQrCodeComponent} from "../../components/tournament-qr-code/tournament-qr-code.component";
import {DatatableColumn} from "@biit-solutions/wizardry-theme/table";
import {combineLatest} from "rxjs";
import {DatePipe} from "@angular/common";
import {ErrorHandler} from "@biit-solutions/wizardry-theme/utils";
import {BiitSnackbarService} from "@biit-solutions/wizardry-theme/info";
import {TableColumnTranslationPipe} from "../../pipes/visualization/table-column-translation-pipe";
import {CustomDatePipe} from "../../pipes/visualization/custom-date-pipe";

@Component({
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
export class TournamentListComponent extends RbacBasedComponent implements OnInit, AfterViewInit {

  protected columns: DatatableColumn[] = [];
  protected pageSize: number = 10;
  protected pageSizes: number[] = [10, 25, 50, 100];
  protected tournaments: Tournament[];
  protected target: Tournament | null;

  protected loading: boolean = false;
  @ViewChildren('booleanCell') booleanCell: QueryList<TemplateRef<any>>;

  constructor(private router: Router, private userSessionService: UserSessionService, private tournamentService: TournamentService,
              private rankingService: RankingService, private translateService: TranslocoService, public dialog: MatDialog,
              private messageService: MessageService, rbacService: RbacService, private systemOverloadService: SystemOverloadService,
              private achievementsService: AchievementsService, private transloco: TranslocoService, private customDatePipe: CustomDatePipe, private _datePipe: DatePipe,
              private biitSnackbarService: BiitSnackbarService, private tableColumnTranslationPipe: TableColumnTranslationPipe) {
    super(rbacService);
  }

  ngOnInit(): void {
  }

  datePipe() {
    return {
      transform: (value: any) => {
        !value ? value = 0 : value;
        return this._datePipe.transform(value, 'dd/MM/yyyy HH:mm:ss');
      }
    }
  }

  ngAfterViewInit() {
    combineLatest(
      [
        this.transloco.selectTranslate('id'),
        this.transloco.selectTranslate('name'),
        this.transloco.selectTranslate('tournamentType'),
        this.transloco.selectTranslate('scoreRules'),
        this.transloco.selectTranslate('locked'),
        this.transloco.selectTranslate('shiaijos'),
        this.transloco.selectTranslate('teamSize'),
        this.transloco.selectTranslate('createdBy'),
        this.transloco.selectTranslate('createdAt'),
        this.transloco.selectTranslate('updatedBy'),
        this.transloco.selectTranslate('updatedAt'),
      ]
    ).subscribe(([id, name, type, scoreRules, locked, shiaijos, teamSize, createdBy, createdAt, updatedBy, updatedAt]) => {
      this.columns = [
        new DatatableColumn(id, 'id', false, 80),
        new DatatableColumn(name, 'name'),
        new DatatableColumn(type, 'type', true, undefined, undefined, this.tableColumnTranslationPipe),
        new DatatableColumn(scoreRules, 'tournamentScore', false, undefined, undefined, this.tableColumnTranslationPipe),
        new DatatableColumn(locked, 'locked', false, 200, undefined, undefined, this.booleanCell.first),
        new DatatableColumn(shiaijos, 'shiaijos', false, 150),
        new DatatableColumn(teamSize, 'teamSize', true, 150),
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
    this.tournamentService.getAll().subscribe({
      next: (_tournaments: Tournament[]): void => {
        this.tournaments = _tournaments.map(_tournament => Tournament.clone(_tournament));
      },
      error: error => ErrorHandler.notify(error, this.transloco, this.biitSnackbarService)
    }).add(() => {
      this.loading = false;
      this.systemOverloadService.isTransactionalBusy.next(false);
    });
  }

  addElement(): void {
    const tournament: Tournament = new Tournament();
    tournament.duelsDuration = Tournament.DEFAULT_DUELS_DURATION;
    tournament.type = Tournament.DEFAULT_TYPE;
    tournament.shiaijos = Tournament.DEFAULT_SHIAIJOS;
    tournament.teamSize = Tournament.DEFAULT_TEAM_SIZE;
    this.openDialog(this.translateService.translate('competitionAdd'), Action.Add, tournament);
  }

  editElement(tournament: Tournament): void {
    if (tournament) {
      this.openDialog(this.translateService.translate('competitionEdit'), Action.Update, tournament);
    }
  }

  deleteElement(tournament: Tournament, confirmed: boolean): void {
    if (tournament) {
      this.openDialog(this.translateService.translate('competitionDelete'), Action.Delete, tournament);
    }
  }

  openDialog(title: string, action: Action, tournament: Tournament): void {
    const dialogRef = this.dialog.open(TournamentDialogBoxComponent, {
      width: '600px',
      data: {
        title: title, action: action, entity: tournament
      }
    });

    dialogRef.afterClosed().subscribe(result => {
      if (result == undefined) {
        //Do nothing
      } else if (result?.action == Action.Add) {
        this.addRowData(result.data);
      } else if (result?.action == Action.Update) {
        this.updateRowData(result.data);
      } else if (result?.action == Action.Delete) {
        this.deleteRowData(result.data);
        this.userSessionService.setSelectedTournament(undefined);
      }
    });
  }

  addRowData(tournament: Tournament): void {
    this.tournamentService.add(tournament).subscribe((_tournament: Tournament): void => {
      //If data is not already added though table webservice.
      this.loadData();
      this.target = null;
      this.messageService.infoMessage('infoTournamentStored');
    });
  }

  updateRowData(tournament: Tournament): void {
    this.tournamentService.update(tournament).subscribe((_tournament: Tournament): void => {
        this.loadData();
        this.target = null;
        this.messageService.infoMessage('infoTournamentUpdated');
      }
    );
  }

  deleteRowData(tournament: Tournament): void {
    this.tournamentService.delete(tournament).subscribe((): void => {
        this.messageService.infoMessage('infoTournamentDeleted');
        this.loadData();
        this.target = null;
      }
    );
  }

  addRoles(tournament: Tournament): void {
    if (tournament) {
      this.dialog.open(TournamentRolesComponent, {
        panelClass: 'pop-up-panel',
        data: {
          tournament: tournament
        }
      });
    }
  }

  addTeams(tournament: Tournament): void {
    if (tournament) {
      this.dialog.open(TournamentTeamsComponent, {
        panelClass: 'pop-up-panel',
        data: {
          tournament: tournament
        }
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
      this.rankingService.getTournamentSummaryAsHtml(tournament.id).subscribe((html: Blob): void => {
        const blob: Blob = new Blob([html], {type: 'txt/plain'});
        const downloadURL: string = window.URL.createObjectURL(blob);

        const anchor = document.createElement("a");
        anchor.download = "Code - " + tournament!.name + ".txt";
        anchor.href = downloadURL;
        anchor.click();
      });
    }
  }

  downloadAccreditations(tournament: Tournament): void {
    if (tournament) {
      const dialogRef: MatDialogRef<RoleSelectorDialogBoxComponent> = this.dialog.open(RoleSelectorDialogBoxComponent, {
        data: {
          tournament: tournament
        }
      });

      dialogRef.afterClosed().subscribe(result => {
        if (result.action !== Action.Cancel) {
          if (tournament?.id) {
            this.tournamentService.getAccreditations(tournament.id, result.newOnes, result.data).subscribe((html: Blob): void => {
              if (html !== null) {
                const blob: Blob = new Blob([html], {type: 'application/pdf'});
                const downloadURL: string = window.URL.createObjectURL(blob);

                const anchor: HTMLAnchorElement = document.createElement("a");
                anchor.download = "Accreditations - " + tournament!.name + ".pdf";
                anchor.href = downloadURL;
                anchor.click();
              } else {
                this.messageService.warningMessage('noResults');
              }
            });
          }
        }
      });
    }
  }

  downloadDiplomas(tournament: Tournament): void {
    if (tournament) {
      const dialogRef: MatDialogRef<RoleSelectorDialogBoxComponent> = this.dialog.open(RoleSelectorDialogBoxComponent, {
        data: {
          tournament: tournament
        }
      });

      dialogRef.afterClosed().subscribe(result => {
        if (result.action !== Action.Cancel) {
          if (tournament?.id) {
            this.tournamentService.getDiplomas(tournament.id, result.newOnes, result.data).subscribe((html: Blob) => {
              if (html !== null) {
                const blob: Blob = new Blob([html], {type: 'application/pdf'});
                const downloadURL: string = window.URL.createObjectURL(blob);

                const anchor: HTMLAnchorElement = document.createElement("a");
                anchor.download = "Diplomas - " + tournament!.name + ".pdf";
                anchor.href = downloadURL;
                anchor.click();
              } else {
                this.messageService.warningMessage('noResults');
              }
            });
          }
        }
      });
    }
  }

  lockElement(tournament: Tournament, locked: boolean): void {
    if (tournament) {
      tournament.locked = locked;
      if (locked) {
        this.achievementsService.regenerateTournamentAchievements(tournament?.id!).subscribe();
        if (!tournament.lockedAt) {
          tournament.lockedAt = new Date();
        }
        if (!tournament.finishedAt) {
          tournament.finishedAt = new Date();
        }
      }
      this.updateRowData(tournament);
    }
  }

  disableRow(argument: any): boolean {
    return (argument as Tournament).locked;
  }

  openStatistics(tournament: Tournament): void {
    if (tournament) {
      this.userSessionService.setSelectedTournament(tournament.id + "");
      this.router.navigate(['/tournaments/statistics'], {state: {tournamentId: tournament.id}});
    }
  }

  openCloneTournament(tournament: Tournament): void {
    if (tournament) {
      let dialogRef: MatDialogRef<ConfirmationDialogComponent> = this.dialog.open(ConfirmationDialogComponent, {
        disableClose: false
      });
      dialogRef.componentInstance.messageTag = "tournamentCloneWarning"

      dialogRef.afterClosed().subscribe(result => {
        if (result) {
          this.cloneElement(tournament);
        }
      });
    }
  }

  cloneElement(tournament: Tournament): void {
    const tournamentId: number = tournament?.id!;
    this.tournamentService.clone(tournamentId).subscribe((_tournament: Tournament): void => {
      this.loadData();
      this.messageService.infoMessage('infoTournamentStored');
    });
  }

  downloadZip(tournament: Tournament): void {
    if (tournament?.id) {
      this.rankingService.getAllListAsZip(tournament.id).subscribe((html: Blob): void => {
        const blob: Blob = new Blob([html], {type: 'application/zip'});
        const downloadURL: string = window.URL.createObjectURL(blob);

        const anchor = document.createElement("a");
        anchor.download = tournament!.name + ".zip";
        anchor.href = downloadURL;
        anchor.click();
      });
    }
  }

  showQrCode(tournament: Tournament): void {
    if (tournament) {
      const dialogRef: MatDialogRef<TournamentQrCodeComponent> = this.dialog.open(TournamentQrCodeComponent, {
        panelClass: 'pop-up-panel',
        data: {
          tournament: tournament,
          port: window.location.port
        }
      });
    }
  }
}
