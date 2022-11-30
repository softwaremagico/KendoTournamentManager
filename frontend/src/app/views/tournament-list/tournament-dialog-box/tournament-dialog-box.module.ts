import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {TournamentDialogBoxComponent} from "./tournament-dialog-box.component";
import {MatFormFieldModule} from "@angular/material/form-field";
import {TranslateModule} from "@ngx-translate/core";
import {MatSelectModule} from "@angular/material/select";
import {FormsModule} from "@angular/forms";
import {MatTooltipModule} from "@angular/material/tooltip";
import {RbacModule} from "../../../pipes/rbac-pipe/rbac.module";


@NgModule({
  declarations: [TournamentDialogBoxComponent],
  imports: [
    CommonModule,
    MatFormFieldModule,
    TranslateModule,
    MatSelectModule,
    FormsModule,
    MatTooltipModule,
    RbacModule
  ]
})
export class TournamentDialogBoxModule {
}
