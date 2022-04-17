import {Component, OnInit} from '@angular/core';

@Component({
  selector: 'user-score',
  templateUrl: './user-score.component.html',
  styleUrls: ['./user-score.component.scss']
})
export class UserScoreComponent implements OnInit {

  constructor() {
  }

  leftToRight: boolean;

  ngOnInit(): void {
  }

}
