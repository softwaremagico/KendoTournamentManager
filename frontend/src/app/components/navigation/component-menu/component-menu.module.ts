import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { ComponentMenuComponent } from './component-menu.component';



@NgModule({
    declarations: [
        ComponentMenuComponent
    ],
    exports: [
        ComponentMenuComponent
    ],
    imports: [
        CommonModule
    ]
})
export class ComponentMenuModule { }
