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
import {Tournament} from "../../models/tournament";

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

    const tournament : Tournament = new Tournament();
    tournament.name = "Liga Interna Abril 2034";


    this.achievements = [];
    const today = new Date();
    let achievement: Achievement = new Achievement();
    achievement.achievementType = AchievementType.BILLY_THE_KID;
    achievement.achievementGrade = AchievementGrade.BRONZE;
    achievement.createdAt = new Date(2023, 1, today.getDate() - 1, 12);
    achievement.tournament=tournament;
    this.achievements.push(achievement);

    achievement = new Achievement();
    achievement.achievementType = AchievementType.BILLY_THE_KID;
    achievement.achievementGrade = AchievementGrade.NORMAL;
    achievement.createdAt.setDate(new Date().setDate(new Date().getDate()));
    achievement.tournament=tournament;
    this.achievements.push(achievement);

    const achievement2: Achievement = new Achievement();
    achievement2.achievementType = AchievementType.JUGGERNAUT;
    achievement2.achievementGrade = AchievementGrade.GOLD;
    achievement2.createdAt = new Date(2023, 1, today.getDate() - 4, 12);
    achievement2.tournament=tournament;
    this.achievements.push(achievement2);

    const achievement3: Achievement = new Achievement();
    achievement3.achievementType = AchievementType.TERMINATOR;
    achievement3.achievementGrade = AchievementGrade.SILVER;
    achievement3.createdAt = new Date(2023, 1, today.getDate() - 1, 12);
    achievement3.tournament=tournament;
    this.achievements.push(achievement3);

    const achievement4: Achievement = new Achievement();
    achievement4.achievementType = AchievementType.ENTRENCHED;
    achievement4.achievementGrade = AchievementGrade.BRONZE;
    achievement4.createdAt = new Date(2023, 1, today.getDate() - 1, 12);
    achievement4.tournament=tournament;
    this.achievements.push(achievement4);
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
