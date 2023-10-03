import {Component, Inject, OnInit, Optional} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {Tournament} from "../../../../models/tournament";
import {RbacService} from "../../../../services/rbac/rbac.service";
import {TranslateService} from "@ngx-translate/core";
import {MessageService} from "../../../../services/message.service";
import {RbacBasedComponent} from "../../../../components/RbacBasedComponent";
import {TournamentType} from "../../../../models/tournament-type";
import {UntypedFormControl} from "@angular/forms";
import {DrawResolution} from "../../../../models/draw-resolution";
import {TournamentExtendedProperty} from "../../../../models/tournament-extended-property.model";
import {TournamentExtraPropertyKey} from "../../../../models/tournament-extra-property-key";
import {TournamentExtendedPropertiesService} from "../../../../services/tournament-extended-properties.service";
import {MatSlideToggleChange} from "@angular/material/slide-toggle";

@Component({
  selector: 'app-tournament-extra-properties',
  templateUrl: './tournament-extra-properties.component.html',
  styleUrls: ['./tournament-extra-properties.component.scss']
})
export class TournamentExtraPropertiesComponent extends RbacBasedComponent implements OnInit {

  tournament: Tournament;
  title: string;

  drawResolution: DrawResolution[];

  //Enable
  needsMaximizeFights: boolean;
  needsDrawResolution: boolean;
  needsFifoWinner: boolean;

  //Values
  areFightsMaximized: boolean;
  firstInFirstOut: boolean;
  selectedDrawResolution: DrawResolution;


  avoidDuplicates = new UntypedFormControl('', []);

  constructor(@Optional() @Inject(MAT_DIALOG_DATA) public data: { title: string, tournament: Tournament },
              public dialogRef: MatDialogRef<TournamentExtraPropertiesComponent>, rbacService: RbacService, public translateService: TranslateService,
              private tournamentExtendedPropertiesService: TournamentExtendedPropertiesService,
              public messageService: MessageService,) {
    super(rbacService);
    this.tournament = data.tournament;
    this.title = data.title;

    this.drawResolution = DrawResolution.toArray();
    this.needsMaximizeFights = TournamentType.canMaximizeFights(this.tournament.type);
    this.needsDrawResolution = TournamentType.needsDrawResolution(this.tournament.type);
    this.needsFifoWinner = TournamentType.needsFifoWinner(this.tournament.type);

    this.defaultPropertiesValue();
  }

  ngOnInit(): void {
    this.tournamentExtendedPropertiesService.getByTournament(this.tournament).subscribe((_tournamentSelection: TournamentExtendedProperty[]): void => {
      if (_tournamentSelection) {
        for (const _tournamentProperty of _tournamentSelection) {
          if (_tournamentProperty.propertyKey == TournamentExtraPropertyKey.KING_DRAW_RESOLUTION) {
            this.selectedDrawResolution = DrawResolution.getByKey(_tournamentProperty.propertyValue);
          }
          if (_tournamentProperty.propertyKey == TournamentExtraPropertyKey.MAXIMIZE_FIGHTS) {
            this.areFightsMaximized = (_tournamentProperty.propertyValue.toLowerCase() == "true");
          }
          if (_tournamentProperty.propertyKey == TournamentExtraPropertyKey.LEAGUE_FIGHTS_ORDER_GENERATION) {
            this.firstInFirstOut = (_tournamentProperty.propertyValue.toLowerCase() == "true");
          }
        }
      }
    });
  }

  defaultPropertiesValue(): void {
    this.areFightsMaximized = TournamentExtraPropertyKey.getDefaultMaximizedFights();
    this.selectedDrawResolution = TournamentExtraPropertyKey.getDefaultKingDrawResolutions();
    this.firstInFirstOut = TournamentExtraPropertyKey.getDefaultLeagueFightsOrderGeneration();
  }

  getDrawResolutionTranslationTag(drawResolution: DrawResolution): string {
    if (!drawResolution) {
      return "";
    }
    return DrawResolution.toCamel(drawResolution);
  }

  getDrawResolutionHintTag(drawResolution: DrawResolution): string {
    if (!drawResolution) {
      return "";
    }
    return DrawResolution.toCamel(drawResolution) + "Hint";
  }


  selectDrawResolution(drawResolution: DrawResolution) {
    this.selectedDrawResolution = drawResolution;
    const tournamentProperty: TournamentExtendedProperty = new TournamentExtendedProperty();
    tournamentProperty.tournament = this.tournament;
    tournamentProperty.propertyValue = drawResolution;
    tournamentProperty.propertyKey = TournamentExtraPropertyKey.KING_DRAW_RESOLUTION;
    this.tournamentExtendedPropertiesService.update(tournamentProperty).subscribe(() => {
      this.messageService.infoMessage('infoTournamentUpdated');
    });
  }

  closeDialog() {
    this.dialogRef.close();
  }

  fifoToggle($event: MatSlideToggleChange): void {
    const tournamentProperty: TournamentExtendedProperty = new TournamentExtendedProperty();
    tournamentProperty.tournament = this.tournament;
    tournamentProperty.propertyValue = $event.checked + "";
    tournamentProperty.propertyKey = TournamentExtraPropertyKey.LEAGUE_FIGHTS_ORDER_GENERATION;
    this.tournamentExtendedPropertiesService.update(tournamentProperty).subscribe((): void => {
      this.messageService.infoMessage('infoTournamentUpdated');
    });
  }

  maxFightsToggle($event: MatSlideToggleChange): void {
    const tournamentProperty: TournamentExtendedProperty = new TournamentExtendedProperty();
    tournamentProperty.tournament = this.tournament;
    tournamentProperty.propertyValue = $event.checked + "";
    tournamentProperty.propertyKey = TournamentExtraPropertyKey.MAXIMIZE_FIGHTS;
    this.tournamentExtendedPropertiesService.update(tournamentProperty).subscribe((): void => {
      this.messageService.infoMessage('infoTournamentUpdated');
    });
  }
}
