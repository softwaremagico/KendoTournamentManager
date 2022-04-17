import {Component, Inject, Input, OnInit} from '@angular/core';
import {Participant} from "../../../../models/participant";

@Component({
  selector: 'user-name',
  templateUrl: './user-name.component.html',
  styleUrls: ['./user-name.component.scss']
})
export class UserNameComponent implements OnInit {

  @Input()
  participant: Participant;

  constructor() {
  }

  ngOnInit(): void {
  }

}
