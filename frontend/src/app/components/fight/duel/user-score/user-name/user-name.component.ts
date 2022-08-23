import {Component, Input, OnChanges, OnInit} from '@angular/core';
import {Participant} from "../../../../../models/participant";
import {debounceTime, fromEvent, Subscription} from "rxjs";
import {NameUtilsService} from "../../../../../services/name-utils.service";

@Component({
  selector: 'user-name',
  templateUrl: './user-name.component.html',
  styleUrls: ['./user-name.component.scss']
})
export class UserNameComponent implements OnInit, OnChanges {

  @Input()
  participant: Participant | undefined;

  @Input()
  left: boolean;

  @Input()
  swapTeams: boolean;

  resizeSubscription$: Subscription;

  public displayName: string = '';

  constructor(public nameUtilsService: NameUtilsService) {

  }

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

  ngOnChanges() {
    this.displayName = this.getDisplayName(window.innerWidth);
  }

  getShortName(): string {
    return this.nameUtilsService.getShortName(this.participant);
  }

  getShortLastName(): string {
    return this.nameUtilsService.getShortLastName(this.participant);
  }

  getLastname(): string {
    return this.nameUtilsService.getLastname(this.participant);
  }

  getDisplayName(resolution: number): string {
    return this.nameUtilsService.getDisplayName(this.participant, resolution);
  }

}
