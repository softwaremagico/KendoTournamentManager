import {Injectable} from '@angular/core';
import {Participant} from "../models/participant";

@Injectable({
  providedIn: 'root'
})
export class NameUtilsService {

  constructor() {
  }

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

  getLastnameName(participant: Participant | undefined): string {
    return this.getLastname(participant) + ", " + this.getShortName(participant);
  }

  getDisplayName(participant: Participant | undefined, resolution: number): string {
    if (resolution > 1500) {
      return this.getLastname(participant) + ', ' + this.getName(participant);
    } else if (resolution > 1200) {
      return this.getLastname(participant) + ', ' + this.getShortName(participant);
    } else if (resolution > 900) {
      return this.getShortLastName(participant) + ', ' + this.getShortName(participant);
    } else {
      return this.getShortLastName(participant);
    }
  }
}
