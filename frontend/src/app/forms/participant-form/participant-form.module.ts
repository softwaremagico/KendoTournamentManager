import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ParticipantFormComponent } from './participant-form.component';
import {BiitDropdownModule, BiitInputTextModule} from "@biit-solutions/wizardry-theme/inputs";
import {HasPermissionPipe} from "../../pipes/has-permission.pipe";
import {MapGetPipeModule} from "@biit-solutions/wizardry-theme/utils";
import {TranslocoModule} from "@ngneat/transloco";
import {BiitButtonModule} from "@biit-solutions/wizardry-theme/button";
import {FormsModule} from "@angular/forms";
import {DropdownInterfacePipeModule} from "../../pipes/dropdown-interface-pipe/dropdown-interface-pipe.module";



@NgModule({
  declarations: [
    ParticipantFormComponent
  ],
  exports: [
    ParticipantFormComponent
  ],
  imports: [
    CommonModule,
    BiitInputTextModule,
    HasPermissionPipe,
    MapGetPipeModule,
    TranslocoModule,
    BiitButtonModule,
    FormsModule,
    BiitDropdownModule,
    DropdownInterfacePipeModule
  ]
})
export class ParticipantFormModule { }
