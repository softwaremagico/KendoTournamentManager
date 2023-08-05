import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import { TournamentGeneratorComponent } from './tournament-generator.component';



@NgModule({
  declarations: [
    TournamentGeneratorComponent
  ],
  exports:[
    TournamentGeneratorComponent
  ],
  imports: [
    CommonModule
  ]
})
export class TournamentGeneratorModule { }
