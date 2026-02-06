import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ResetPasswordComponent } from './reset-password.component';
import {TranslocoModule} from "@ngneat/transloco";
import {BiitInputTextModule} from "@biit-solutions/wizardry-theme/inputs";
import {FormsModule} from "@angular/forms";
import {BiitButtonModule} from "@biit-solutions/wizardry-theme/button";
import {RbacModule} from "../../pipes/rbac-pipe/rbac.module";
import {HasPermissionPipe} from "../../pipes/has-permission.pipe";
import {MapGetPipeModule} from "@biit-solutions/wizardry-theme/utils";
import {BiitProgressBarModule} from "@biit-solutions/wizardry-theme/info";



@NgModule({
  declarations: [
    ResetPasswordComponent
  ],
  exports: [
    ResetPasswordComponent
  ],
  imports: [
    CommonModule,
    TranslocoModule,
    BiitInputTextModule,
    FormsModule,
    BiitButtonModule,
    RbacModule,
    HasPermissionPipe,
    MapGetPipeModule,
    BiitProgressBarModule
  ]
})
export class ResetPasswordModule { }
