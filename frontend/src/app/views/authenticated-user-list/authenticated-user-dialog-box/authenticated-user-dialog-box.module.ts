import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {AuthenticatedUserDialogBoxComponent} from "./authenticated-user-dialog-box.component";
import {ReactiveFormsModule} from "@angular/forms";
import {MatFormFieldModule} from "@angular/material/form-field";
import {TranslateModule} from "@ngx-translate/core";
import {MatOptionModule} from "@angular/material/core";
import {RbacModule} from "../../../pipes/rbac-pipe/rbac.module";
import {MatIconModule} from "@angular/material/icon";
import {MatSelectModule} from "@angular/material/select";
import {MatInputModule} from "@angular/material/input";
import {MatButtonModule} from "@angular/material/button";
import {MatDialogModule} from "@angular/material/dialog";


@NgModule({
  declarations: [AuthenticatedUserDialogBoxComponent],
  imports: [
    CommonModule,
    ReactiveFormsModule,
    MatFormFieldModule,
    TranslateModule,
    MatOptionModule,
    RbacModule,
    MatIconModule,
    MatSelectModule,
    MatInputModule,
    MatButtonModule,
    MatDialogModule
  ]
})
export class AuthenticatedUserDialogBoxModule { }
