import {Component, Inject, OnInit, Optional} from '@angular/core';
import {RbacBasedComponent} from "../../../../components/RbacBasedComponent";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {RbacService} from "../../../../services/rbac/rbac.service";
import {Tournament} from "../../../../models/tournament";

@Component({
  selector: 'app-tournament-image-selector',
  templateUrl: './tournament-image-selector.component.html',
  styleUrls: ['./tournament-image-selector.component.scss']
})
export class TournamentImageSelectorComponent extends RbacBasedComponent implements OnInit {
  tournament: Tournament;

  constructor(@Optional() @Inject(MAT_DIALOG_DATA) public data: { tournament: Tournament },
              public dialogRef: MatDialogRef<TournamentImageSelectorComponent>, rbacService: RbacService) {
    super(rbacService);
    this.tournament = data.tournament;
  }

  ngOnInit(): void {
  }

}
