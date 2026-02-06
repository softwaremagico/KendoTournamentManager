import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { SenbatsuFightCreatorComponent } from './senbatsu-fight-creator.component';
import {BiitButtonModule} from "@biit-solutions/wizardry-theme/button";
import {CdkDropList, CdkDropListGroup} from "@angular/cdk/drag-drop";
import {TeamCardModule} from "../team-card/team-card.module";
import {TeamListModule} from "../basic/team-list/team-list.module";
import {TranslocoModule} from "@ngneat/transloco";



@NgModule({
  declarations: [
    SenbatsuFightCreatorComponent
  ],
  exports: [
    SenbatsuFightCreatorComponent
  ],
  imports: [
    CommonModule,
    BiitButtonModule,
    CdkDropList,
    CdkDropListGroup,
    TeamCardModule,
    TeamListModule,
    TranslocoModule
  ]
})
export class SenbatsuFightCreatorModule { }
