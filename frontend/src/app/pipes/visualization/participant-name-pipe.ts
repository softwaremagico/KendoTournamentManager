import {Pipe, PipeTransform} from "@angular/core";
import {Participant} from "../../models/participant";
import {NameUtilsService} from "../../services/name-utils.service";

@Pipe({
  name: 'participantNamePipe',
  standalone: true
})
export class ParticipantNamePipe implements PipeTransform {

  constructor(private readonly nameUtilsService: NameUtilsService) {

  }


  transform(value: Participant | undefined): string {
    if (value) {
      return this.nameUtilsService.getDisplayName(value);
    }
    return '';
  }
}
