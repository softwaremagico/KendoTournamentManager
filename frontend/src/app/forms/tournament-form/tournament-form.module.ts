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
    BiitButtonModule
  ]
})
export class TournamentFormModule { }
