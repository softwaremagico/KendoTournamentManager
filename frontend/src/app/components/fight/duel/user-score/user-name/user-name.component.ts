import {Component, Input, OnInit} from '@angular/core';
import {Participant} from "../../../../../models/participant";
import {debounceTime, fromEvent, Subscription} from "rxjs";

@Component({
  selector: 'user-name',
  templateUrl: './user-name.component.html',
  styleUrls: ['./user-name.component.scss']
})
export class UserNameComponent implements OnInit {

  @Input()
  participant: Participant | undefined;

  @Input()
  left: boolean;

  resizeSubscription$: Subscription;

  public displayName: string = '';

  ngOnInit(): void {
    this.resizeSubscription$ = fromEvent(window, 'resize').pipe(debounceTime(200))
      .subscribe(event => {
        this.displayName = this.getDisplayName(window.innerWidth);
      });
    this.displayName = this.getDisplayName(window.innerWidth);
  }

  ngOnDestroy() {
    this.resizeSubscription$.unsubscribe()
  }

  getShortName(): string {
    if (!this.participant) return "";
    return this.participant.name.slice(0, 1).toUpperCase() + ".";
  }

  getShortLastName(): string {
    if (!this.participant) return "";
    let capital: number = 0;
    for (let i = 0; i < this.participant.lastname.length; i++) {
      if (this.participant.lastname[i] !== ' ' && this.participant.lastname[i].toUpperCase() === this.participant.lastname[i]) {
        capital = i;
        break;
      }
    }
    let lastNameEnd = this.participant.lastname.indexOf(' ', capital);
    if (lastNameEnd <= 0) {
      lastNameEnd = this.participant.lastname.length;
    }
    return this.participant.lastname.substring(0, lastNameEnd);
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

  getDisplayName(resolution: number): string {
    if (resolution > 1200) {
      return this.getLastname() + ', ' + this.getShortName();
    } else if (resolution > 900) {
      return this.getShortLastName() + ', ' + this.getShortName();
    } else {
      return this.getShortLastName();
    }
  }

}
