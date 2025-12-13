import {AfterViewInit, Component} from '@angular/core';
import {MatDialog} from "@angular/material/dialog";
import {TRANSLOCO_SCOPE, TranslocoService} from "@ngneat/transloco";
import {AuthenticatedUser} from "../../models/authenticated-user";
import {UserService} from "../../services/user.service";
import {UserRoles} from "../../services/rbac/user-roles";
import {RbacBasedComponent} from "../../components/RbacBasedComponent";
import {RbacService} from "../../services/rbac/rbac.service";
import {DatatableColumn} from "@biit-solutions/wizardry-theme/table";
import {SystemOverloadService} from "../../services/notifications/system-overload.service";
import {ErrorHandler} from "@biit-solutions/wizardry-theme/utils";
import {BiitSnackbarService, NotificationType} from "@biit-solutions/wizardry-theme/info";
import {combineLatest} from "rxjs";
import {UserSessionService} from "../../services/user-session.service";
import {DatePipe} from "@angular/common";
import {Constants} from "../../constants";

@Component({
  selector: 'app-authenticated-user-list',
  templateUrl: './authenticated-user-list.component.html',
  styleUrls: ['./authenticated-user-list.component.scss'],
  providers: [
    {
      provide: TRANSLOCO_SCOPE,
      multi: true,
      useValue: {scope: '', alias: 't'}
    }
  ]
})
export class AuthenticatedUserListComponent extends RbacBasedComponent implements AfterViewInit {

  protected readonly AuthenticatedUser = AuthenticatedUser;

  protected columns: DatatableColumn[] = [];
  protected pageSize: number = 10;
  protected pageSizes: number[] = [10, 25, 50, 100];
  protected users: AuthenticatedUser[];
  protected target: AuthenticatedUser | null;
  protected confirm: boolean = false;

  protected loading: boolean = false;

  constructor(private userService: UserService, public dialog: MatDialog,
              rbacService: RbacService, private systemOverloadService: SystemOverloadService, private userSessionService: UserSessionService,
              private transloco: TranslocoService, private biitSnackbarService: BiitSnackbarService,
              private _datePipe: DatePipe) {
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
        this.transloco.selectTranslate('username'),
        this.transloco.selectTranslate('name'),
        this.transloco.selectTranslate('lastname'),
        this.transloco.selectTranslate('roles'),
        this.transloco.selectTranslate('createdBy'),
        this.transloco.selectTranslate('createdAt'),
        this.transloco.selectTranslate('updatedBy'),
        this.transloco.selectTranslate('updatedAt'),
      ]
    ).subscribe(([id, username, name, lastname, roles, createdBy, createdAt, updatedBy, updatedAt]) => {
      this.columns = [
        new DatatableColumn(id, 'id', false, 80),
        new DatatableColumn(name, 'name'),
        new DatatableColumn(lastname, 'lastname'),
        new DatatableColumn(username, 'username'),
        new DatatableColumn(roles, 'roles'),
        new DatatableColumn(createdBy, 'createdBy', false),
        new DatatableColumn(createdAt, 'createdAt', undefined, undefined, undefined, this.datePipe()),
        new DatatableColumn(updatedBy, 'updatedBy', false),
        new DatatableColumn(updatedAt, 'updatedAt', false, undefined, undefined, this.datePipe())
      ];
      this.loadData();
    });
  }

  loadData(): void {
    this.loading = true;
    this.systemOverloadService.isTransactionalBusy.next(true);
    this.userService.getAll().subscribe({
      next: (_users: AuthenticatedUser[]): void => {
        this.users = _users.map(_user => AuthenticatedUser.clone(_user));
      },
      error: error => ErrorHandler.notify(error, this.transloco, this.biitSnackbarService)
    }).add(() => {
      this.loading = false;
      this.systemOverloadService.isTransactionalBusy.next(false);
    });
  }

  addElement(): void {
    const authenticatedUser: AuthenticatedUser = new AuthenticatedUser();
    authenticatedUser.roles[0] = UserRoles.VIEWER;
    this.target = authenticatedUser;
  }

  editElement(authenticatedUser: AuthenticatedUser): void {
    if (authenticatedUser) {
      this.target = authenticatedUser;
    }
  }

  deleteElement(authenticatedUsers: AuthenticatedUser[], confirmed: boolean): void {
    if (authenticatedUsers.some(user => user.username === this.userSessionService.getUser()?.username)) {
      this.biitSnackbarService.showNotification(this.transloco.translate('youCannotDeleteYourself'), NotificationType.WARNING);
      return;
    }
    if (authenticatedUsers) {
      combineLatest(authenticatedUsers.map(authenticatedUser => this.userService.delete(authenticatedUser))).subscribe({
        next: (): void => {
          this.confirm = false;
          this.loadData();
          this.transloco.selectTranslate('infoAuthenticatedUserDeleted').subscribe(
            translation => {
              this.biitSnackbarService.showNotification(translation, NotificationType.SUCCESS);
            }
          );
        },
        error: error => ErrorHandler.notify(error, this.transloco, this.biitSnackbarService)
      });
    }
  }

  getUserNames(authenticatedUsers: AuthenticatedUser[]): string {
    if (authenticatedUsers) {
      return authenticatedUsers.map(authenticatedUser => authenticatedUser.username).join(', ');
    }
    return "";
  }

  onSaved(authenticatedUser: AuthenticatedUser) {
    //Saved already on the popup.
    this.biitSnackbarService.showNotification(this.transloco.translate('infoAuthenticatedUserStored'), NotificationType.INFO);
    this.loadData();
    this.target = null;
  }
}
