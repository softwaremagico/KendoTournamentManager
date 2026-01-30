import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { AuthenticatedUserFormComponent } from './authenticated-user-form.component';
import {TranslocoModule} from "@ngneat/transloco";
import {BiitDropdownModule, BiitInputTextModule} from "@biit-solutions/wizardry-theme/inputs";
import {MapGetPipeModule} from "@biit-solutions/wizardry-theme/utils";
import {HasPermissionPipe} from "../../pipes/has-permission.pipe";
import {FormsModule} from "@angular/forms";
import {BiitButtonModule} from "@biit-solutions/wizardry-theme/button";
import {BiitTabGroupModule} from "@biit-solutions/wizardry-theme/navigation";



@NgModule({
  declarations: [
    AuthenticatedUserFormComponent
  ],
  exports: [
    AuthenticatedUserFormComponent
  ],
  imports: [
    CommonModule,
    TranslocoModule,
    BiitInputTextModule,
    MapGetPipeModule,
    BiitDropdownModule,
    HasPermissionPipe,
    FormsModule,
    BiitButtonModule,
    BiitTabGroupModule
  ]
})
export class AuthenticatedUserFormModule { }
