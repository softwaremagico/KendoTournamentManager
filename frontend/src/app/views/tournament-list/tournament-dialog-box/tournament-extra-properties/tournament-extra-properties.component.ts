import {Component, Inject, Optional} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {Tournament} from "../../../../models/tournament";
import {RbacService} from "../../../../services/rbac/rbac.service";
import {TranslateService} from "@ngx-translate/core";
import {MessageService} from "../../../../services/message.service";
import {RbacBasedComponent} from "../../../../components/RbacBasedComponent";

@Component({
  selector: 'app-tournament-extra-properties',
  templateUrl: './tournament-extra-properties.component.html',
  styleUrls: ['./tournament-extra-properties.component.scss']
})
export class TournamentExtraPropertiesComponent extends RbacBasedComponent {

  tournament: Tournament;
  title: string;

  constructor(@Optional() @Inject(MAT_DIALOG_DATA) public data: { title: string, tournament: Tournament },
              public dialogRef: MatDialogRef<TournamentExtraPropertiesComponent>, rbacService: RbacService, public translateService: TranslateService,
              public messageService: MessageService,) {
    super(rbacService);
    this.tournament = data.tournament;
    this.title = data.title;
  }


}
