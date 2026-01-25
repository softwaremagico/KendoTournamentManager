import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TournamentImagesComponent } from './tournament-images.component';
import {BiitProgressBarModule} from "@biit-solutions/wizardry-theme/info";
import {BiitTabGroupModule} from "@biit-solutions/wizardry-theme/navigation";
import {TranslocoRootModule} from "@biit-solutions/wizardry-theme/i18n";
import {HasPermissionPipe} from "../../../../pipes/has-permission.pipe";
import {MatIconModule} from "@angular/material/icon";
import {FormsModule} from "@angular/forms";
import {MatLegacySliderModule} from "@angular/material/legacy-slider";
import {BiitSliderOptionVerticalModule} from "@biit-solutions/wizardry-theme/inputs";
import {BiitButtonModule, BiitIconButtonModule} from "@biit-solutions/wizardry-theme/button";
import {FloatingDeleteButtonModule} from "../../../../components/floating-delete-button/floating-delete-button.module";
import {MatButtonModule} from "@angular/material/button";



@NgModule({
  declarations: [
    TournamentImagesComponent
  ],
  exports: [
    TournamentImagesComponent
  ],
  imports: [
    CommonModule,
    BiitProgressBarModule,
    BiitTabGroupModule,
    TranslocoRootModule,
    HasPermissionPipe,
    MatButtonModule,
    MatIconModule,
    FormsModule,
    MatLegacySliderModule,
    BiitSliderOptionVerticalModule,
    BiitButtonModule,
    BiitIconButtonModule,
    FloatingDeleteButtonModule
  ]
})
export class TournamentImagesModule { }
