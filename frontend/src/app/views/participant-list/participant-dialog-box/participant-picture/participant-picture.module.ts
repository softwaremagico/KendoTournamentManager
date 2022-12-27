import { NgModule } from '@angular/core';
import { CommonModule } from '@angular/common';
import {ParticipantPictureComponent} from "./participant-picture.component";
import {WebcamModule} from "ngx-webcam";



@NgModule({
  declarations: [ParticipantPictureComponent],
  imports: [
    CommonModule,
    WebcamModule
  ]
})
export class ParticipantPictureModule { }
