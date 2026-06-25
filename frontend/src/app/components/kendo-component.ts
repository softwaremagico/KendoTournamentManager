import {Component, OnDestroy} from "@angular/core";
import {Subject} from "rxjs";

@Component({
  standalone: false,
  template: ''
})
export abstract class KendoComponent implements OnDestroy {
  destroySubject: Subject<boolean> = new Subject<boolean>();

  ngOnDestroy(): void {
    this.destroySubject.next(true);
    this.destroySubject.complete();
  }
}
