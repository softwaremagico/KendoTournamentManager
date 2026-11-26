import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TournamentExtraPropertiesFormComponent} from './tournament-extra-properties-form.component';
import {MatSpinnerOverlayModule} from "../../components/mat-spinner-overlay/mat-spinner-overlay.module";
import {TranslocoModule} from "@jsverse/transloco";
import {BiitDropdownModule, BiitInputTextModule, BiitToggleModule} from "@biit-solutions/wizardry-theme/inputs";
import {FormsModule} from "@angular/forms";
import {HasPermissionPipe} from "../../pipes/has-permission.pipe";
import {DropdownInterfacePipeModule} from "../../pipes/dropdown-interface-pipe/dropdown-interface-pipe.module";


@NgModule({
  declarations: [
    TournamentExtraPropertiesFormComponent
  ],
  exports: [
    TournamentExtraPropertiesFormComponent
  ],
  imports: [
    CommonModule,
    MatSpinnerOverlayModule,
    TranslocoModule,
    BiitToggleModule,
    FormsModule,
    HasPermissionPipe,
    BiitDropdownModule,
    BiitInputTextModule,
    DropdownInterfacePipeModule
  ]
})
export class TournamentExtraPropertiesFormModule { }
