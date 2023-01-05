import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {ParticipantPictureComponent} from "./participant-picture.component";


@NgModule({
  declarations: [ParticipantPictureComponent],
  exports: [
    ParticipantPictureComponent
  ],
  imports: [
    CommonModule
  ]
})
export class ParticipantPictureModule {

}
