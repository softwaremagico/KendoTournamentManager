import {Component, OnInit} from '@angular/core';

import {LoginService} from "../../services/login.service";
import {ActivatedRoute, Router} from "@angular/router";
import {UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {MessageService} from "../../services/message.service";
import {LoggerService} from "../../services/logger.service";
import {AuthenticatedUser} from "../../models/authenticated-user";
import { HttpHeaders } from "@angular/common/http";
import {InfoService} from "../../services/info.service";
import {TranslocoService} from '@jsverse/transloco';
import {EnvironmentService} from "../../environment.service";
import {BiitProgressBarType} from "@biit-solutions/wizardry-theme/info";
import {BiitLogin} from "@biit-solutions/wizardry-theme/models";
import {UserSessionService} from "../../services/user-session.service";
import {Constants} from "../../constants";
import {ActivityService} from "../../services/rbac/activity.service";
import {TenantService} from '../../services/tenant.service';
import packageJson from '../../../../package.json';

const appVersion: string = packageJson.version;

@Component({
  standalone: false,
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  username: string;
  password: string;
  tenant: string = '';
  tenants: string[] = [];
  loginForm: UntypedFormGroup;
  appVersion: string;
  protected waiting: boolean = true;
  protected readonly BiitProgressBarType = BiitProgressBarType;

  protected checkForNewVersion: boolean = this.environmentService.isCheckForNewVersion();


  constructor(private router: Router, private activatedRoute: ActivatedRoute, private loginService: LoginService,
              private formBuilder: UntypedFormBuilder, private messageService: MessageService, private loggerService: LoggerService,
              private infoService: InfoService, private translateService: TranslocoService, private environmentService: EnvironmentService,
               private userSessionService: UserSessionService, private activityService: ActivityService, private translocoService: TranslocoService,
               private tenantService: TenantService) {
    this.appVersion = appVersion;
    this.loginForm = this.formBuilder.group({
      username: ['', Validators.email],
      password: ['', Validators.required]
    });
  }


  ngOnInit(): void {
    this.isLastVersion();
    this.managePathQueries();
    this.tenant = localStorage.getItem('tenant') ?? '';
    this.tenantService.getActiveNames().subscribe({
      next: tenants => {
        this.tenants = tenants;
        if (tenants.length === 1) {
          this.tenant = tenants[0];
        }
      },
      error: () => this.tenants = []
    });
    if (!this.userSessionService.isTokenExpired()) {
      this.router.navigate([Constants.PATHS.TOURNAMENTS.ROOT]);
    } else {
      this.waiting = false;
      this.userSessionService.clearToken();
    }
  }

  showTenantSelector(): boolean {
    // Show the selector only when there are multiple tenants.
    // Hide when there are 0 or 1 tenants.
    return this.tenants.length > 1;
  }

  isLastVersion(): void {
    if (this.checkForNewVersion) {
      //Get last version and compare with current one.
      this.infoService.getLatestVersion().subscribe((_version: string): void => {
        if (_version && this.appVersion !== _version) {
          const parameters: object = {currentVersion: this.appVersion, newVersion: _version};
          this.messageService.warningMessage(this.translateService.translate('newVersionAvailable', parameters));
        }
      })
    }
  }

  private managePathQueries(): void {
    this.activatedRoute.queryParams.subscribe(params => {
      const queryParams: { [key: string]: string | null } = {};
      if (params[Constants.PATHS.QUERY.LOGOUT] !== undefined) {
        this.loginService.logout();
        this.activityService.clear()
        this.translocoService.selectTranslate(Constants.PATHS.QUERY.LOGOUT, {}, {scope: 'wizardry-theme/utils'}).subscribe(msg => {
          this.messageService.infoMessage(msg);
        });
        queryParams[Constants.PATHS.QUERY.LOGOUT] = null;
      }
      this.router.navigate([], {queryParams: queryParams, queryParamsHandling: 'merge'});
    });
  }

  login(login: BiitLogin): void {
    this.waiting = true;
    const tenant: string = this.tenant;
    // Tenant is optional: call login even when tenant is empty
    this.loginService.login(login.username, login.password, tenant).subscribe({
      next: (authenticatedUser: AuthenticatedUser): void => {
        this.loginService.setAuthenticatedUser(authenticatedUser, (): void => {});

        this.activityService.setRoles(authenticatedUser.roles);
        const returnUrl = this.activatedRoute.snapshot.queryParams["returnUrl"];
        if (returnUrl) {
          this.router.navigate([returnUrl]);
        } else {
          this.router.navigate([Constants.PATHS.TOURNAMENTS.ROOT]);
        }
        this.messageService.infoMessage("userLoggedInMessage");
        this.userSessionService.setUser(AuthenticatedUser.clone(authenticatedUser))
        localStorage.setItem('username', (this.loginForm.controls['username'].value));
        // Store tenant only when provided, otherwise remove any previous value
        if (tenant) {
          localStorage.setItem('tenant', tenant);
        } else {
          localStorage.removeItem('tenant');
        }
      },
      error: (error): void => {
        if (error.status === 401) {
          this.loggerService.info(`Error logging: ` + error);
          this.messageService.errorMessage("deniedUserError");
        } else if (error.status === 423) {
          this.loggerService.info(`Blocked IP!: ` + error);
          this.messageService.warningMessage("blockedUserError");
        } else if (error.status === 400) {
          console.error(error);
        } else {
          console.error(error);
          this.messageService.errorMessage("backendError");
        }
      }
    }).add(() => this.waiting = false);
  }

  onResetPassword(email: string) {

  }
}
