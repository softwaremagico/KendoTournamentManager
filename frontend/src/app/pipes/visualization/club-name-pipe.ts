import {Pipe, PipeTransform} from "@angular/core";
import {Club} from "../../models/club";

@Pipe({
  name: 'clubNamePipe',
  standalone: true
})
export class ClubNamePipe implements PipeTransform {

  constructor() {

  }


  transform(value: Club): any {
    if (value) {
      return value.name;
    }
    return '';
  }
}
