import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {LoginComponent} from './login.component';
import {BiitLoginModule} from "@biit-solutions/wizardry-theme/login";
import {BiitProgressBarModule} from "@biit-solutions/wizardry-theme/info";
import {TranslocoRootModule} from "@biit-solutions/wizardry-theme/i18n";
import {LoginRoutingModule} from "./login-routing.module";


@NgModule({
  declarations: [
    LoginComponent
  ],
  exports: [
    LoginComponent
  ],
  imports: [
    LoginRoutingModule,
    CommonModule,
    BiitLoginModule,
    TranslocoRootModule,
    BiitProgressBarModule
  ]
})
export class LoginModule {
}
