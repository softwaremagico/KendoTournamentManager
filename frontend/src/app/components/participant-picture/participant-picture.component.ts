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

}
