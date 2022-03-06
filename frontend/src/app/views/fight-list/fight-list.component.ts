import { Component, OnInit } from '@angular/core';
import {MatDialog} from "@angular/material/dialog";
import {MessageService} from "../../services/message.service";
import {FightService} from "../../services/fight.service";

@Component({
  selector: 'app-fight-list',
  templateUrl: './fight-list.component.html',
  styleUrls: ['./fight-list.component.scss']
})
export class FightListComponent implements OnInit {

  constructor(private fightService: FightService, public dialog: MatDialog, private messageService: MessageService) { }

  ngOnInit(): void {
  }

  addElement() {

  }
}
