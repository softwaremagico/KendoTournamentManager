import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {BasicTableComponent} from './basic-table.component';
import {TranslocoModule} from "@ngneat/transloco";
import {MatIconModule} from "@angular/material/icon";
import {MatMenuModule} from "@angular/material/menu";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatTableModule} from "@angular/material/table";
import {MatInputModule} from "@angular/material/input";
import {MatSortModule} from "@angular/material/sort";
import {FilterModule} from "../filter/filter.module";
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
    TranslocoModule,
    MatIconModule,
    MatMenuModule,
    MatCheckboxModule,
    MatPaginatorModule,
    MatTableModule,
    MatInputModule,
    MatSortModule,
    MatButtonModule,
    FilterModule
  ]
})
export class BasicTableModule { }
