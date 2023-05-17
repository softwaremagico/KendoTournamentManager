import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ClubDialogBoxComponent} from "./club-dialog-box.component";
import {MatFormFieldModule} from "@angular/material/form-field";
import {TranslateModule} from "@ngx-translate/core";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {RbacModule} from "../../../pipes/rbac-pipe/rbac.module";
import {MatInputModule} from "@angular/material/input";
import {MatDialogModule} from "@angular/material/dialog";
import {MatButtonModule} from "@angular/material/button";
import {MatIconModule} from "@angular/material/icon";


@NgModule({
  declarations: [ClubDialogBoxComponent],
  imports: [
    CommonModule,
    MatFormFieldModule,
    TranslateModule,
    FormsModule,
    RbacModule,
    MatInputModule,
    MatDialogModule,
    MatButtonModule,
    ReactiveFormsModule,
    MatIconModule
  ]
})
export class ClubDialogBoxModule { }
