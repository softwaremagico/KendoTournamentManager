import {Component, Input, OnInit} from '@angular/core';
import {ProgressSpinnerMode} from "@angular/material/progress-spinner";
import {ThemePalette} from "@angular/material/core/common-behaviors/color";
import {SystemOverloadService} from "../../services/system-overload.service";

@Component({
  selector: 'app-mat-spinner-overlay',
  templateUrl: './mat-spinner-overlay.component.html',
  styleUrls: ['./mat-spinner-overlay.component.scss']
})
export class MatSpinnerOverlayComponent implements OnInit {

  constructor(private systemOverloadService: SystemOverloadService) {
  }

  @Input() value: number = 100;
  @Input() diameter: number = 100;
  @Input() mode: ProgressSpinnerMode = 'indeterminate';
  @Input() strokeWidth: number = 10;
  @Input() overlay: boolean = false;
  @Input() color: ThemePalette = "primary";

  showSpinner = false;

  ngOnInit() {
    this.systemOverloadService.isBusy.subscribe(busy => {
      this.showSpinner = busy;
    });
  }

}
