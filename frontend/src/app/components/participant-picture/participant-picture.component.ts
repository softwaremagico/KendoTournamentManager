import {Component, Input, OnInit} from '@angular/core';
import {Participant} from "../../models/participant";
import {FileService} from "../../services/file.service";
import {NameUtilsService} from "../../services/name-utils.service";

@Component({
  selector: 'app-participant-picture',
  templateUrl: './participant-picture.component.html',
  styleUrls: ['./participant-picture.component.scss']
})
export class ParticipantPictureComponent implements OnInit {

  @Input()
  participant: Participant;

  participantPicture: string | undefined;
  participantInitials: string;

  constructor(private fileService: FileService, private nameUtils: NameUtilsService) {
  }

  ngOnInit(): void {
    this.fileService.getPicture(this.participant).subscribe(_picture => {
      if (_picture) {
        this.participantPicture = _picture.base64;
      } else {
        this.participantPicture = undefined;
      }
    });
    this.participantInitials = this.nameUtils.getInitials(this.participant);
  }

  public get circleStyle(): string {
    return "background-color: " + this.getBackgroundColor(this.participant) + " color:" + this.getFontColor(this.participant);
  }

  private getBackgroundColor(participant: Participant): string {
    let color = 'rgb(';
    if (participant.id) {
      const seed = Math.abs(participant.id);
      const mainColor = seed % 135 + 120;
      const secondaryColor = mainColor > 170 && mainColor < 205 ? mainColor - 50 + seed % 100 :
        (mainColor < 205 ? mainColor + seed % 50 : mainColor - seed % 50);
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
      const mainColor = seed % 90;
      const secondaryColor = 90 - seed % 90;
      const thirdColor = mainColor > 10 ? mainColor - 10 + seed % 20 : mainColor + seed % 20;
      color += (seed % 3 == 0 ? mainColor : seed % 3 == 1 ? secondaryColor : thirdColor) + ", ";
      color += (seed % 3 == 1 ? mainColor : seed % 3 == 2 ? secondaryColor : thirdColor) + ", ";
      color += (seed % 3 == 2 ? mainColor : seed % 3 == 0 ? secondaryColor : thirdColor);
    } else {
      color += "255, 255, 255";
    }
    color += ');';
    return color;
  }

}
