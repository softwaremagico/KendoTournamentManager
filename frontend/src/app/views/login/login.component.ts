import {Component} from '@angular/core';

import {LoginService} from "../../services/login.service";
import {ActivatedRoute, Router} from "@angular/router";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {MessageService} from "../../services/message.service";
import {LoggerService} from "../../services/logger.service";
import {RbacService} from "../../services/rbac/rbac.service";
import {AuthenticatedUser} from "../../models/authenticated-user";
import {Group} from "../../models/group";

const {version: appVersion} = require('../../../../package.json')

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  username: string;
  password: string;
  loginForm: FormGroup;
  appVersion: string;
  groups: Group[];
  relations: Map<number, { src: number, dest: number }[]>;

  constructor(private router: Router, private activatedRoute: ActivatedRoute, private loginService: LoginService, private rbacService: RbacService,
              private formBuilder: FormBuilder, private messageService: MessageService, private loggerService: LoggerService) {
    this.appVersion = appVersion;
    this.loginForm = this.formBuilder.group({
      username: ['', Validators.email],
      password: ['', Validators.required]
    });

    //TODO(softwaremagico): delete this demo data.
    this.groups = [];
    for (let i = 0; i < 7; i++) {
      const group: Group = new Group();
      group.level = i < 4 ? 0 : (i < 6 ? 1 : 2);
      this.groups.push(group)
    }
    this.relations = new Map<number, [{ src: number; dest: number }]>();
    this.relations.set(0, [{src: 0, dest: 0}, {src: 1, dest: 0}, {src: 2, dest:1}, {src: 3, dest:1}]);
    this.relations.set(1, [{src: 0, dest: 0}, {src: 1, dest: 0}]);
  }

  login() {
    this.loginService.login(this.loginForm.controls['username'].value, this.loginForm.controls['password'].value).subscribe({
      next: (authenticatedUser: AuthenticatedUser): void => {
        this.loginService.setJwtValue(authenticatedUser.jwt, authenticatedUser.expires);
        this.loginService.autoRenewToken(authenticatedUser.jwt, authenticatedUser.expires, (jwt: string, expires: number): void => {
        });
        this.rbacService.setRoles(authenticatedUser.roles);
        let returnUrl = this.activatedRoute.snapshot.queryParams["returnUrl"];
        this.router.navigate([returnUrl]);
        this.messageService.infoMessage("userloggedInMessage");
        localStorage.setItem('username', (this.loginForm.controls['username'].value));
      },
      error: (error): void => {
        if (error.includes(401)) {
          this.loggerService.info(`Error logging: ` + error);
          this.messageService.errorMessage("deniedUserError");
        } else if (error.includes(423)) {
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
