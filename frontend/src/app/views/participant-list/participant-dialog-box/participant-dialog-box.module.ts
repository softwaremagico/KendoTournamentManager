import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {ParticipantDialogBoxComponent} from "./participant-dialog-box.component";
import {MatFormFieldModule} from "@angular/material/form-field";
import {TranslateModule} from "@ngx-translate/core";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {MatIconModule} from "@angular/material/icon";
import {RbacModule} from "../../../pipes/rbac-pipe/rbac.module";
import {MatInputModule} from "@angular/material/input";
import {MatDialogModule} from "@angular/material/dialog";
import {MatButtonModule} from "@angular/material/button";
import { ParticipantPictureDialogBoxComponent } from './participant-picture/participant-picture-dialog-box.component';
import {ParticipantPictureModule} from "../../../components/participant-picture/participant-picture.module";



@NgModule({
  declarations: [ParticipantDialogBoxComponent],
  imports: [
    CommonModule,
    MatFormFieldModule,
    TranslateModule,
    FormsModule,
    MatAutocompleteModule,
    ReactiveFormsModule,
    MatIconModule,
    RbacModule,
    MatInputModule,
    MatDialogModule,
    MatButtonModule,
    ParticipantPictureModule
  ]
})
export class ParticipantDialogBoxModule { }
