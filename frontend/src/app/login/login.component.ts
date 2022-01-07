import {Component, NgModule} from '@angular/core';

import {AuthenticatedUserService} from "../services/authenticated-user.service";
import {ActivatedRoute, Router} from "@angular/router";
import {FormBuilder, FormGroup, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {MessageService} from "../services/message.service";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  username: string;
  password: string;
  loginForm: FormGroup;
  public loginInvalid = false;
  private formSubmitAttempt = false;

  constructor(private router: Router, private activatedRoute: ActivatedRoute, public authenticatedUserService: AuthenticatedUserService,
              private formBuilder: FormBuilder, private messageService : MessageService) {
    this.loginForm = this.formBuilder.group({
      username: ['', Validators.email],
      password: ['', Validators.required]
    });
  }

  login() {
    this.authenticatedUserService.login(this.username, this.password).subscribe(data => {
        this.authenticatedUserService.setJwtValue(data.jwt);

        let returnUrl = this.activatedRoute.snapshot.queryParams["returnUrl"];
        this.router.navigate([returnUrl]);

      },
      err => {
        this.loginInvalid = true;
        this.messageService.errorMessage("deniedUser");
      });
  }
}
