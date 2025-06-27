import {Component, OnInit} from '@angular/core';

import {LoginService} from "../../services/login.service";
import {ActivatedRoute, Router} from "@angular/router";
import {UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {MessageService} from "../../services/message.service";
import {LoggerService} from "../../services/logger.service";
import {RbacService} from "../../services/rbac/rbac.service";
import {AuthenticatedUser} from "../../models/authenticated-user";
import {HttpHeaders} from "@angular/common/http";
import {InfoService} from "../../services/info.service";
import {TranslateService} from "@ngx-translate/core";
import {environment} from "../../../environments/environment";

const {version: appVersion} = require('../../../../package.json')

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {
  username: string;
  password: string;
  loginForm: UntypedFormGroup;
  appVersion: string;

  protected checkForNewVersion: boolean = JSON.parse(String(environment.checkForNewVersion));


  constructor(private router: Router, private activatedRoute: ActivatedRoute, private loginService: LoginService, private rbacService: RbacService,
              private formBuilder: UntypedFormBuilder, private messageService: MessageService, private loggerService: LoggerService,
              private infoService: InfoService, private translateService: TranslateService) {
    this.appVersion = appVersion;
    this.loginForm = this.formBuilder.group({
      username: ['', Validators.email],
      password: ['', Validators.required]
    });
  }


  ngOnInit(): void {
    this.isLastVersion();
  }

  isLastVersion(): void {
    if (this.checkForNewVersion) {
      //Get last version and compare with current one.
      const headers = new HttpHeaders().set('x-skip-auth', "true");
      this.infoService.getLatestVersion().subscribe((_version: string): void => {
        if (_version && this.appVersion != _version) {
          const parameters: object = {currentVersion: this.appVersion, newVersion: _version};
          this.translateService.get('newVersionAvailable', parameters).subscribe((message: string): void => {
            this.messageService.warningMessage(message);
          });
        }
      })
    }
  }

  login(): void {
    this.loginService.login(this.loginForm.controls['username'].value, this.loginForm.controls['password'].value).subscribe({
      next: (authenticatedUser: AuthenticatedUser): void => {
        this.loginService.setAuthenticatedUser(authenticatedUser, (jwt: string, expires: number): void => {
        });

        const returnUrl = this.activatedRoute.snapshot.queryParams["returnUrl"];
        this.router.navigate([returnUrl]);
        this.messageService.infoMessage("userloggedInMessage");
        localStorage.setItem('username', (this.loginForm.controls['username'].value));
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
    });
  }
}
