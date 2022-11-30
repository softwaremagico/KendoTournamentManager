import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {ClubDialogBoxComponent} from "./club-dialog-box.component";
import {MatFormFieldModule} from "@angular/material/form-field";
import {TranslateModule} from "@ngx-translate/core";
import {FormsModule} from "@angular/forms";
import {RbacModule} from "../../../pipes/rbac-pipe/rbac.module";



@NgModule({
  declarations: [ClubDialogBoxComponent],
  imports: [
    CommonModule,
    MatFormFieldModule,
    TranslateModule,
    FormsModule,
    RbacModule
  ]
})
export class ClubDialogBoxModule { }
