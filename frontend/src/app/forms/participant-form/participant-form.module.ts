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
import {FloatingDeleteButtonModule} from "../../components/floating-delete-button/floating-delete-button.module";
import {BiitPopupModule} from "@biit-solutions/wizardry-theme/popup";
import {
  ParticipantPictureDialogModule
} from "../../views/participant-list/participant-form-popup/participant-picture/participant-picture-dialog-box.module";



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
    DropdownInterfacePipeModule,
    FloatingDeleteButtonModule,
    BiitPopupModule,
    ParticipantPictureDialogModule
  ]
})
export class ParticipantFormModule { }
