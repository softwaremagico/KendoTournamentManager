import {Component} from '@angular/core';

import {LoginService} from "../../services/login.service";
import {ActivatedRoute, Router} from "@angular/router";
import {UntypedFormBuilder, UntypedFormGroup, Validators} from "@angular/forms";
import {MessageService} from "../../services/message.service";
import {LoggerService} from "../../services/logger.service";
import {RbacService} from "../../services/rbac/rbac.service";
import {AuthenticatedUser} from "../../models/authenticated-user";

const {version: appVersion} = require('../../../../package.json')

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  username: string;
  password: string;
  loginForm: UntypedFormGroup;
  appVersion: string;


  constructor(private router: Router, private activatedRoute: ActivatedRoute, private loginService: LoginService, private rbacService: RbacService,
              private formBuilder: UntypedFormBuilder, private messageService: MessageService, private loggerService: LoggerService) {
    this.appVersion = appVersion;
    this.loginForm = this.formBuilder.group({
      username: ['', Validators.email],
      password: ['', Validators.required]
    });
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
        } else {
          console.error(error);
          this.messageService.errorMessage("backendError");
        }
      }
    });
  }
}
