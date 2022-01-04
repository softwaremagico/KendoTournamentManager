import {Component} from '@angular/core';

import {AuthenticatedUserService} from "../services/authenticated-user.service";
import {ActivatedRoute, Router} from "@angular/router";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  email: string;
  password: string;

  constructor(private router: Router, private activatedRoute: ActivatedRoute, public authenticatedUserService: AuthenticatedUserService) {
  }

  login() {
    this.authenticatedUserService.login(this.email, this.password).subscribe(data => {
      this.authenticatedUserService.setToken(data.jwt);

      this.activatedRoute.queryParams.subscribe((params: any) => {
        const returnUrl = params['returnUrl'];
        this.router.navigate([returnUrl]);
      });
    });
  }
}
