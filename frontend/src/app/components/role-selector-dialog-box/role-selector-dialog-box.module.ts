import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {RoleSelectorDialogBoxComponent} from "./role-selector-dialog-box.component";
import {MatDialogModule} from "@angular/material/dialog";
import {MatSelectModule} from "@angular/material/select";
import {ReactiveFormsModule} from "@angular/forms";
import {TranslocoModule} from "@ngneat/transloco";
import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {BiitButtonModule} from "@biit-solutions/wizardry-theme/button";


@NgModule({
  declarations: [RoleSelectorDialogBoxComponent],
  exports: [
    RoleSelectorDialogBoxComponent
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
    BiitButtonModule
  ]
})
export class RoleSelectorDialogBoxModule {
}
