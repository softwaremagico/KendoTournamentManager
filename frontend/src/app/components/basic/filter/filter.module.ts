import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FilterComponent} from './filter.component';
import {FormsModule} from "@angular/forms";
import {TranslocoModule} from "@ngneat/transloco";
import {MatInputModule} from "@angular/material/input";
import {MatIconModule} from "@angular/material/icon";
import {MatButtonModule} from "@angular/material/button";


@NgModule({
  declarations: [
    FilterComponent
  ],
  exports: [
    FilterComponent
  ],
    imports: [
        CommonModule,
        FormsModule,
        TranslocoModule,
        MatInputModule,
        MatIconModule,
        MatButtonModule
    ]
})
export class FilterModule { }
