import {Injectable} from '@angular/core';
import {Participant} from "../models/participant";

@Injectable({
  providedIn: 'root'
})
export class NameUtilsService {

  getName(participant: Participant | undefined): string {
    if (!participant) return "";
    return participant.name;
  }

  getShortName(participant: Participant | undefined): string {
    if (!participant) return "";
    return participant.name.slice(0, 1).toUpperCase() + ".";
  }

  getShortLastName(participant: Participant | undefined): string {
    if (!participant) return "";
    let capital: number = 0;
    for (let i = 0; i < participant.lastname.length; i++) {
      if (participant.lastname[i] !== ' ' && participant.lastname[i].toUpperCase() === participant.lastname[i]) {
        capital = i;
        break;
      }
    }
    let lastNameEnd = participant.lastname.indexOf(' ', capital);
    if (lastNameEnd <= 0) {
      lastNameEnd = participant.lastname.length;
    }
    return participant.lastname.substring(0, lastNameEnd);
  }

  getLastname(participant: Participant | undefined): string {
    if (!participant) return "";
    let lastnames: string[] = participant.lastname.split(" ");
    let finalResult: string[] = [];
    for (let lastname of lastnames) {
      finalResult.push(lastname.length < 3 ? lastname : (lastname[0].toUpperCase() + lastname.substring(1).toLowerCase()));
    }
    return finalResult.join(" ");
  }

  getLastnameNameCamelCase(participant: Participant | undefined): string {
    if (!participant) return "";
    return this.spacesToCamel(participant.lastname + " " + this.getShortName(participant));
  }

  getLastnameNameNoSpaces(participant: Participant | undefined): string {
    return this.getLastname(participant) + ", " + this.getShortName(participant);
  }

  getAcronym(participant: Participant | undefined): string {
    if (!participant) return "";
    return participant.lastname.slice(0, 1) + ". " + participant.name.slice(0, 1) + ".";
  }

  getInitials(participant: Participant | undefined): string {
    if (!participant) return "";
    let nameUpperIndex: number = 0;
    let lastnameUpperIndex: number = 0;
    for (let i = 0; i < participant.name.length; i++) {
      if (participant.name[i] >= 'A' && participant.name[i] <= 'Z') {
        nameUpperIndex = i;
        break;
      }
    }
    for (let i = 0; i < participant.lastname.length; i++) {
      if (participant.lastname[i] >= 'A' && participant.lastname[i] <= 'Z') {
        lastnameUpperIndex = i;
        break;
      }
    }
    return participant.name.slice(nameUpperIndex, 1) + participant.lastname.slice(lastnameUpperIndex, 1);
  }

  getDisplayName(participant: Participant | undefined, resolution?: number): string {
    if (!resolution || resolution > 1500) {
      return this.getLastname(participant) + ', ' + this.getName(participant);
    } else if (resolution > 1200) {
      return this.getLastname(participant) + ', ' + this.getShortName(participant);
    } else if (resolution > 1000) {
      return this.getShortLastName(participant) + ', ' + this.getShortName(participant);
    } else if (resolution > 900) {
      return this.getShortLastName(participant);
    } else {
      return this.getAcronym(participant);
    }
  }


  spacesToCamel(value: string) {
    return value.toLowerCase()
      .replace(/ (.)/g, function ($1) {
        return $1.toUpperCase();
      })
      .replace(/ /g, '');
  }
}
