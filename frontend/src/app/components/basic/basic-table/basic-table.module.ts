import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {BasicTableComponent} from './basic-table.component';
import {TranslateModule} from "@ngx-translate/core";
import {MatIconModule} from "@angular/material/icon";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatMenuModule} from "@angular/material/menu";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatTableModule} from "@angular/material/table";
import {MatInputModule} from "@angular/material/input";
import {MatSortModule} from "@angular/material/sort";
import {MatButtonModule} from "@angular/material/button";

@NgModule({
  declarations: [
    BasicTableComponent
  ],
  exports: [
    BasicTableComponent
  ],
  imports: [
    CommonModule,
    TranslateModule,
    MatIconModule,
    MatFormFieldModule,
    MatMenuModule,
    MatCheckboxModule,
    MatPaginatorModule,
    MatTableModule,
    MatInputModule,
    MatSortModule,
    MatButtonModule
  ]
})
export class BasicTableModule { }
