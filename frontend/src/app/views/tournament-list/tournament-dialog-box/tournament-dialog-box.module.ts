import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TournamentDialogBoxComponent} from "./tournament-dialog-box.component";
import {MatFormFieldModule} from "@angular/material/form-field";
import {TranslateModule} from "@ngx-translate/core";
import {MatSelectModule} from "@angular/material/select";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatTooltipModule} from "@angular/material/tooltip";
import {RbacModule} from "../../../pipes/rbac-pipe/rbac.module";
import {MatDialogModule} from "@angular/material/dialog";
import {MatButtonModule} from "@angular/material/button";
import {MatInputModule} from "@angular/material/input";
import {MatIconModule} from "@angular/material/icon";


@NgModule({
  declarations: [TournamentDialogBoxComponent],
  imports: [
    CommonModule,
    MatFormFieldModule,
    TranslateModule,
    MatSelectModule,
    FormsModule,
    MatTooltipModule,
    RbacModule,
    MatDialogModule,
    MatButtonModule,
    MatInputModule,
    MatIconModule,
    ReactiveFormsModule
  ]
})
export class TournamentDialogBoxModule {
}
