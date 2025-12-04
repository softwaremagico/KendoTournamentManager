import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ParticipantDialogBoxComponent} from "./participant-dialog-box.component";
import {MatFormFieldModule} from "@angular/material/form-field";
import {TranslocoModule} from "@ngneat/transloco";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {MatIconModule} from "@angular/material/icon";
import {RbacModule} from "../../../pipes/rbac-pipe/rbac.module";
import {MatInputModule} from "@angular/material/input";
import {MatDialogModule} from "@angular/material/dialog";
import {MatButtonModule} from "@angular/material/button";
import {ParticipantPictureModule} from "../../../components/participant-picture/participant-picture.module";
import {MatSelectModule} from "@angular/material/select";
import {HasPermissionPipe} from "../../../pipes/has-permission.pipe";


@NgModule({
  declarations: [ParticipantDialogBoxComponent],
    imports: [
        CommonModule,
        MatFormFieldModule,
        TranslocoModule,
        FormsModule,
        MatAutocompleteModule,
        ReactiveFormsModule,
        MatIconModule,
        RbacModule,
        MatInputModule,
        MatDialogModule,
        MatButtonModule,
        ParticipantPictureModule,
        MatSelectModule,
        HasPermissionPipe
    ]
})
export class ParticipantDialogBoxModule { }
