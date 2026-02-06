import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthenticatedUserRolePopupComponent } from './authenticated-user-role-popup.component';
import {AuthenticatedUserFormModule} from "../../../forms/authenticated-user-form/authenticated-user-form.module";
import {BiitPopupModule} from "@biit-solutions/wizardry-theme/popup";
import {TranslocoModule} from "@ngneat/transloco";
import {BiitDropdownModule} from "@biit-solutions/wizardry-theme/inputs";
import {DropdownInterfacePipeModule} from "../../../pipes/dropdown-interface-pipe/dropdown-interface-pipe.module";
import {HasPermissionPipe} from "../../../pipes/has-permission.pipe";
import {FormsModule} from "@angular/forms";
import {BiitButtonModule} from "@biit-solutions/wizardry-theme/button";



@NgModule({
    declarations: [
        AuthenticatedUserRolePopupComponent
    ],
    exports: [
        AuthenticatedUserRolePopupComponent
    ],
  imports: [
    CommonModule,
    AuthenticatedUserFormModule,
    BiitPopupModule,
    TranslocoModule,
    BiitDropdownModule,
    DropdownInterfacePipeModule,
    HasPermissionPipe,
    FormsModule,
    BiitButtonModule
  ]
})
export class AuthenticatedUserRolePopupModule { }
