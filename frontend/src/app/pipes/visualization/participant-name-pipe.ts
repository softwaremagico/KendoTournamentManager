import {Pipe, PipeTransform} from "@angular/core";
import {Participant} from "../../models/participant";
import {NameUtilsService} from "../../services/name-utils.service";

@Pipe({
  name: 'participantNamePipe',
  standalone: true
})
export class ParticipantNamePipe implements PipeTransform {

  constructor(private nameUtilsService: NameUtilsService) {

  }


  transform(value: Participant | undefined): any {
    if (value) {
      return this.nameUtilsService.getDisplayName(value);
    }
    return '';
  }
}
