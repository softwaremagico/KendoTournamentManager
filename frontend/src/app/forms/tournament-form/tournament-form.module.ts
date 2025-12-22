import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TournamentFormComponent } from './tournament-form.component';
import {TranslocoRootModule} from "@biit-solutions/wizardry-theme/i18n";
import {BiitDropdownModule, BiitInputTextModule, BiitMultiselectModule} from "@biit-solutions/wizardry-theme/inputs";
import {FormsModule} from "@angular/forms";
import {MapGetPipeModule} from "@biit-solutions/wizardry-theme/utils";
import {DropdownInterfacePipeModule} from "../../pipes/dropdown-interface-pipe/dropdown-interface-pipe.module";
import {HasPermissionPipe} from "../../pipes/has-permission.pipe";
import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {MatFormFieldModule} from "@angular/material/form-field";
import {BiitButtonModule, BiitIconButtonModule} from "@biit-solutions/wizardry-theme/button";
import {
  NumberDropdownInterfacePipeModule
} from "../../pipes/number-dropdown-interface-pipe/number-dropdown-interface-pipe.module";
import {BiitPopupModule} from "@biit-solutions/wizardry-theme/popup";
import {
    ParticipantPictureDialogModule
} from "../../views/participant-list/participant-form-popup/participant-picture/participant-picture-dialog-box.module";
import {
  TournamentImagesModule
} from "../../views/tournament-list/tournament-form-popup/tournament-images/tournament-images.module";
import {
    TournamentExtraPropertiesFormModule
} from "../tournament-extra-properties-form/tournament-extra-properties-form.module";



@NgModule({
  declarations: [
    TournamentFormComponent
  ],
  exports: [
    TournamentFormComponent
  ],
    imports: [
        CommonModule,
        TranslocoRootModule,
        BiitInputTextModule,
        FormsModule,
        MapGetPipeModule,
        BiitMultiselectModule,
        BiitDropdownModule,
        DropdownInterfacePipeModule,
        HasPermissionPipe,
        MatButtonModule,
        MatIconModule,
        MatFormFieldModule,
        BiitIconButtonModule,
        NumberDropdownInterfacePipeModule,
        BiitButtonModule,
        BiitPopupModule,
        ParticipantPictureDialogModule,
        TournamentImagesModule,
        TournamentExtraPropertiesFormModule
    ]
})
export class TournamentFormModule { }
