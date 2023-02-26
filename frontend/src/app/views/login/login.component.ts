import {Component} from '@angular/core';

import {LoginService} from "../../services/login.service";
import {ActivatedRoute, Router} from "@angular/router";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {MessageService} from "../../services/message.service";
import {LoggerService} from "../../services/logger.service";
import {RbacService} from "../../services/rbac/rbac.service";
import {Achievement} from "../../models/achievement.model";
import {AchievementType} from "../../models/achievement-type.model";
import {AchievementGrade} from "../../models/achievement-grade.model";

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
  achievements: Achievement[];

  constructor(private router: Router, private activatedRoute: ActivatedRoute, private loginService: LoginService, private rbacService: RbacService,
              private formBuilder: FormBuilder, private messageService: MessageService, private loggerService: LoggerService) {
    this.appVersion = appVersion;
    this.loginForm = this.formBuilder.group({
      username: ['', Validators.email],
      password: ['', Validators.required]
    });

    this.achievements = [];
    const today = new Date();
    let achievement: Achievement = new Achievement();
    achievement.achievementType = AchievementType.BILLY_THE_KID;
    achievement.achievementGrade = AchievementGrade.NORMAL;
    achievement.createdAt.setDate(new Date().setDate(today.getDate() - 2));
    this.achievements.push(achievement);

    achievement = new Achievement();
    achievement.achievementType = AchievementType.BILLY_THE_KID;
    achievement.achievementGrade = AchievementGrade.NORMAL;
    achievement.createdAt.setDate(new Date().setDate(new Date().getDate()));
    this.achievements.push(achievement);

    const achievement2: Achievement = new Achievement();
    achievement2.achievementType = AchievementType.JUGGERNAUT;
    achievement2.achievementGrade = AchievementGrade.NORMAL;
    achievement.createdAt.setDate(new Date().setDate(today.getDate() - 5));
    this.achievements.push(achievement2);
  }

  login() {
    this.loginService.login(this.loginForm.controls['username'].value, this.loginForm.controls['password'].value).subscribe({
      next: (authenticatedUser) => {
        this.loginService.setJwtValue(authenticatedUser.jwt);
        this.rbacService.setRoles(authenticatedUser.roles);
        let returnUrl = this.activatedRoute.snapshot.queryParams["returnUrl"];
        this.router.navigate([returnUrl]);
        this.messageService.infoMessage("userloggedInMessage");
        localStorage.setItem('username', (this.loginForm.controls['username'].value));
      },
      error: (error) => {
        if (error.status === 401) {
          this.loggerService.info(`Error logging: ` + error);
          this.messageService.errorMessage("deniedUserError");
        } else {
          console.error(error);
          this.messageService.errorMessage("backendError");
        }
      }
    });
  }
}
