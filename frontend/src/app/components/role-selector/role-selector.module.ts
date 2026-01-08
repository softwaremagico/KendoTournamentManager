import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RoleSelectorComponent} from "./role-selector.component";
import {MatDialogModule} from "@angular/material/dialog";
import {MatSelectModule} from "@angular/material/select";
import {ReactiveFormsModule} from "@angular/forms";
import {TranslocoModule} from "@ngneat/transloco";
import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {BiitButtonModule} from "@biit-solutions/wizardry-theme/button";
import {BiitCheckboxModule} from "@biit-solutions/wizardry-theme/inputs";


@NgModule({
  declarations: [RoleSelectorComponent],
  exports: [
    RoleSelectorComponent
  ],
  imports: [
    CommonModule,
    MatDialogModule,
    MatSelectModule,
    ReactiveFormsModule,
    TranslocoModule,
    MatButtonModule,
    MatIconModule,
    MatCheckboxModule,
    BiitButtonModule,
    BiitCheckboxModule
  ]
})
export class RoleSelectorModule {
}
