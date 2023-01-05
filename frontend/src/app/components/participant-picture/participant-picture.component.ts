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
    return "background-color: " + this.getParticipantColor(this.participant);
  }

  getParticipantColor(participant: Participant): string {
    let color = 'rgb(';
    if (participant.id) {
      const minR = participant.id % 2 == 0 ? 200 : 128;
      const minG = participant.id % 3 == 0 ? 128 : 200;
      const minB = participant.id % 2 == 1 ? 200 : 128;
      color += ((Math.abs(participant.id)) % minR + (256 - minR)) + ", ";
      color += ((Math.abs((participant.id * 31) + participant.id * (participant.id % 2 == 0 ? 1 : -1)) % minG + (256 - minG))) + ", ";
      color += ((Math.abs(participant.id) + participant.id * (participant.id % 2 == 0 ? -1 : 1)) % minB + (256 - minB));
    } else {
      color += "255, 255, 255";
    }
    color += ');';
    return color;
  }

}
