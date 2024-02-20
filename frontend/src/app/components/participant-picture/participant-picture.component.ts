import {Component, Input, OnInit} from '@angular/core';
import {Participant} from "../../models/participant";
import {FileService} from "../../services/file.service";
import {NameUtilsService} from "../../services/name-utils.service";
import {PictureDialogBoxComponent} from "./picture-dialog-box/picture-dialog-box.component";
import {MatDialog} from "@angular/material/dialog";
import {ParticipantImage} from "../../models/participant-image.model";

@Component({
  selector: 'app-participant-picture',
  templateUrl: './participant-picture.component.html',
  styleUrls: ['./participant-picture.component.scss']
})
export class ParticipantPictureComponent implements OnInit {

  @Input()
  participant: Participant | undefined;

  participantPicture: string | undefined;
  participantInitials: string;

  constructor(private fileService: FileService, private nameUtils: NameUtilsService, public dialog: MatDialog) {
  }

  ngOnInit(): void {
    if (this.participant?.hasAvatar) {
      this.fileService.getParticipantPicture(this.participant).subscribe((_picture: ParticipantImage): void => {
        if (_picture) {
          this.participantPicture = _picture.base64;
        } else {
          this.participantPicture = undefined;
        }
      });
    } else {
      this.participantPicture = undefined;
    }
    if (this.participant) {
      this.participantInitials = this.nameUtils.getInitials(this.participant);
    }
  }

  public get circleStyle(): string {
    if (this.participant) {
      return "background-color: " + this.getBackgroundColor(this.participant) + " color:" + this.getFontColor(this.participant);
    } else {
      return "";
    }
  }

  private getBackgroundColor(participant: Participant): string {
    let color = 'rgb(';
    if (participant.id) {
      const seed = Math.abs(participant.id);
      const mainColor = seed % 135 + 120;
      const secondaryColor = mainColor > 170 && mainColor < 205 ? (mainColor % 2 == 0 ? mainColor - 50 + seed % 30 : mainColor + 20 + seed % 30) :
        (mainColor < 205 ? mainColor + 50 - seed % 30 : mainColor - 50 + seed % 30);
      const thirdColor = mainColor + ((participant.id % 25) * (seed % 2 == 0 || seed > 230 ? -1 : 1));
      color += (seed % 3 == 0 ? mainColor : seed % 3 == 1 ? secondaryColor : thirdColor) + ", ";
      color += (seed % 3 == 1 ? mainColor : seed % 3 == 2 ? secondaryColor : thirdColor) + ", ";
      color += (seed % 3 == 2 ? mainColor : seed % 3 == 0 ? secondaryColor : thirdColor);
    } else {
      color += "255, 255, 255";
    }
    color += ');';
    return color;
  }

  private getFontColor(participant: Participant): string {
    let color = 'rgb(';
    if (participant.id) {
      const seed = Math.abs(participant.id);
      const mainColor = seed % 100;
      const secondaryColor = 100 - seed % 100;
      const thirdColor = mainColor > 10 ? mainColor - 10 + seed % 30 : mainColor + seed % 30;
      color += (seed % 3 == 0 ? mainColor : seed % 3 == 1 ? secondaryColor : thirdColor) + ", ";
      color += (seed % 3 == 1 ? mainColor : seed % 3 == 2 ? secondaryColor : thirdColor) + ", ";
      color += (seed % 3 == 2 ? mainColor : seed % 3 == 0 ? secondaryColor : thirdColor);
    } else {
      color += "255, 255, 255";
    }
    color += ');';
    return color;
  }

  openImage() {
    if (this.participantPicture) {
      this.openDialog("", this.participantPicture);
    }
  }

  openDialog(title: string, image: string) {
    this.dialog.open(PictureDialogBoxComponent, {
      width: '435px',
      data: {
        image: image
      }
    });
  }
}
