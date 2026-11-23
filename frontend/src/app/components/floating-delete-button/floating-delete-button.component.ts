import {ChangeDetectionStrategy, Component, Input} from '@angular/core';
import {biitIcon} from "@biit-solutions/biit-icons-collection";

@Component({
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false,
  selector: 'floating-delete-button',
  templateUrl: './floating-delete-button.component.html',
  styleUrls: ['./floating-delete-button.component.scss']
})
export class FloatingDeleteButtonComponent {
  @Input() icon: biitIcon;
  @Input() checked: boolean = false;
}
