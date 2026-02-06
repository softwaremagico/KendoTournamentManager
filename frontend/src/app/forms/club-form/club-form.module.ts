import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ClubFormComponent } from './club-form.component';
import {TranslocoModule} from "@ngneat/transloco";
import {BiitInputTextModule} from "@biit-solutions/wizardry-theme/inputs";
import {MapGetPipeModule} from "@biit-solutions/wizardry-theme/utils";
import {FormsModule} from "@angular/forms";
import {BiitButtonModule} from "@biit-solutions/wizardry-theme/button";
import {HasPermissionPipe} from "../../pipes/has-permission.pipe";



@NgModule({
  declarations: [
    ClubFormComponent
  ],
  exports: [
    ClubFormComponent
  ],
  imports: [
    CommonModule,
    TranslocoModule,
    BiitInputTextModule,
    MapGetPipeModule,
    FormsModule,
    BiitButtonModule,
    HasPermissionPipe
  ]
})
export class ClubFormModule { }
