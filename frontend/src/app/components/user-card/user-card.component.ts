import {Component, Input, OnInit} from '@angular/core';
import {Participant} from "../../models/participant";

@Component({
  selector: 'app-user-card',
  templateUrl: './user-card.component.html',
  styleUrls: ['./user-card.component.scss']
})
export class UserCardComponent implements OnInit {

  @Input()
  user: Participant;

  constructor() {
  }

  ngOnInit(): void {
  }

}
