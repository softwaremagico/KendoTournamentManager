import {Component} from '@angular/core';

import {AuthenticatedUserService} from "../services/authenticated-user.service";

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent {
  email: string;
  password: string;

  constructor(public authenticatedUserService: AuthenticatedUserService) {
  }

  login() {
    this.authenticatedUserService.login(this.email, this.password).subscribe(data => {
      this.authenticatedUserService.setToken(data.jwt);
      console.log(data)
    });
  }
}
