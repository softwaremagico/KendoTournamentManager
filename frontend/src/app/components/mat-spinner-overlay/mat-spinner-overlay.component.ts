import {Component, Input, OnInit} from '@angular/core';
import {ProgressSpinnerMode} from "@angular/material/progress-spinner";
import {ThemePalette} from "@angular/material/core/common-behaviors/color";
import {SystemOverloadService} from "../../services/notifications/system-overload.service";
import {KendoComponent} from "../kendo-component";
import {takeUntil} from "rxjs";

@Component({
  selector: 'app-mat-spinner-overlay',
  templateUrl: './mat-spinner-overlay.component.html',
  styleUrls: ['./mat-spinner-overlay.component.scss']
})
export class MatSpinnerOverlayComponent extends KendoComponent implements OnInit {

  constructor(private systemOverloadService: SystemOverloadService) {
    super();
  }

  @Input() value: number = 100;
  @Input() diameter: number = 100;
  @Input() mode: ProgressSpinnerMode = 'indeterminate';
  @Input() strokeWidth: number = 10;
  @Input() overlay: boolean = false;
  @Input() color: ThemePalette = "primary";

  showSpinner = false;
  waitBigOperation = false;

  ngOnInit() {
    this.systemOverloadService.isBusy.pipe(takeUntil(this.destroySubject)).subscribe(busy => {
      this.showSpinner = busy;
    });
    this.systemOverloadService.isTransactionalBusy.pipe(takeUntil(this.destroySubject)).subscribe(busy => {
      this.waitBigOperation = busy;
    });
  }

}
