import {Component, Input, OnInit} from '@angular/core';
import {Participant} from "../../../../models/participant";

@Component({
  selector: 'user-name',
  templateUrl: './user-name.component.html',
  styleUrls: ['./user-name.component.scss']
})
export class UserNameComponent implements OnInit {

  @Input()
  participant: Participant | undefined;

  constructor() {
  }

  ngOnInit(): void {
  }

  getShortName(): string {
    if (!this.participant) return "";
    return this.participant.name.slice(0, 1).toUpperCase() + ".";
  }

  getLastname(): string {
    if (!this.participant) return "";
    let lastnames: string[] = this.participant.lastname.split(" ");
    let finalResult: string[] = [];
    for (let lastname of lastnames) {
      finalResult.push(lastname.length < 3 ? lastname : (lastname[0].toUpperCase() + lastname.substring(1).toLowerCase()));
    }
    return finalResult.join(" ");
  }

}
