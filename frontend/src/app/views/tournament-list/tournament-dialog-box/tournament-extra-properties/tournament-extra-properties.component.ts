import {Component, Inject, OnInit, Optional} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {Tournament} from "../../../../models/tournament";
import {RbacService} from "../../../../services/rbac/rbac.service";
import {TranslateService} from "@ngx-translate/core";
import {MessageService} from "../../../../services/message.service";
import {RbacBasedComponent} from "../../../../components/RbacBasedComponent";
import {TournamentType} from "../../../../models/tournament-type";
import {DrawResolution} from "../../../../models/draw-resolution";
import {TournamentExtendedProperty} from "../../../../models/tournament-extended-property.model";
import {TournamentExtraPropertyKey} from "../../../../models/tournament-extra-property-key";
import {TournamentExtendedPropertiesService} from "../../../../services/tournament-extended-properties.service";
import {MatSlideToggleChange} from "@angular/material/slide-toggle";
import {LeagueFightsOrder} from "../../../../models/league-fights-order";

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
  canMaximizeFights: boolean;
  needsDrawResolution: boolean;
  canSelectChallengeDistance: boolean;
  needsFifoWinner: boolean;
  canResolveOddFightsAsap: boolean;
  canAvoidDuplicatedFights: boolean;

  //Values
  areFightsMaximized: boolean;
  firstInFirstOut: boolean;
  selectedDrawResolution: DrawResolution;
  avoidDuplicatedFights: boolean;
  resolveOddFightsAsap: boolean;
  challengeDistance: number;

  constructor(@Optional() @Inject(MAT_DIALOG_DATA) public data: { title: string, tournament: Tournament },
              public dialogRef: MatDialogRef<TournamentExtraPropertiesComponent>, rbacService: RbacService, public translateService: TranslateService,
              private tournamentExtendedPropertiesService: TournamentExtendedPropertiesService,
              public messageService: MessageService,) {
    super(rbacService);
    this.tournament = data.tournament;
    this.title = data.title;

    this.drawResolution = DrawResolution.toArray();
    this.canMaximizeFights = TournamentType.canMaximizeFights(this.tournament.type);
    this.needsDrawResolution = TournamentType.needsDrawResolution(this.tournament.type);
    this.canSelectChallengeDistance = TournamentType.canSelectChallengeDistance(this.tournament.type);
    this.needsFifoWinner = TournamentType.needsFifoWinner(this.tournament.type);
    this.canAvoidDuplicatedFights = TournamentType.avoidsDuplicatedFights(this.tournament.type);
    this.canResolveOddFightsAsap = TournamentType.resolveOddFightsAsap(this.tournament.type);

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
            this.firstInFirstOut = (_tournamentProperty.propertyValue.toUpperCase() == LeagueFightsOrder.FIFO);
          }
          if (_tournamentProperty.propertyKey == TournamentExtraPropertyKey.AVOID_DUPLICATES) {
            this.avoidDuplicatedFights = (_tournamentProperty.propertyValue.toLowerCase() == "true");
          }
          if (_tournamentProperty.propertyKey == TournamentExtraPropertyKey.ODD_FIGHTS_RESOLVED_ASAP) {
            this.resolveOddFightsAsap = (_tournamentProperty.propertyValue.toLowerCase() == "true");
          }
          if (_tournamentProperty.propertyKey == TournamentExtraPropertyKey.SENBATSU_CHALLENGE_DISTANCE) {
            this.challengeDistance = isNaN(+_tournamentProperty.propertyValue) ? TournamentExtraPropertyKey.senbatsuChallengeDistance() : Number(_tournamentProperty.propertyValue);
          }
        }
      }
    });
  }

  defaultPropertiesValue(): void {
    this.areFightsMaximized = TournamentExtraPropertyKey.getDefaultMaximizedFights();
    this.selectedDrawResolution = TournamentExtraPropertyKey.getDefaultKingDrawResolutions();
    this.firstInFirstOut = TournamentExtraPropertyKey.getDefaultLeagueFightsOrderGeneration();
    this.avoidDuplicatedFights = TournamentExtraPropertyKey.avoidDuplicateFightsGeneration();
    this.resolveOddFightsAsap = TournamentExtraPropertyKey.oddFightsResolvedAsap();
    this.challengeDistance = TournamentExtraPropertyKey.senbatsuChallengeDistance();
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


  selectDrawResolution(drawResolution: DrawResolution): void {
    this.selectedDrawResolution = drawResolution;
    const tournamentProperty: TournamentExtendedProperty = new TournamentExtendedProperty();
    tournamentProperty.tournament = this.tournament;
    tournamentProperty.propertyValue = drawResolution;
    tournamentProperty.propertyKey = TournamentExtraPropertyKey.KING_DRAW_RESOLUTION;
    this.tournamentExtendedPropertiesService.update(tournamentProperty).subscribe((): void => {
      this.messageService.infoMessage('infoTournamentUpdated');
    });
  }

  selectChallengeDistance(challengeDistance: number): void {
    this.challengeDistance = challengeDistance;
    const tournamentProperty: TournamentExtendedProperty = new TournamentExtendedProperty();
    tournamentProperty.tournament = this.tournament;
    tournamentProperty.propertyValue = challengeDistance + "";
    tournamentProperty.propertyKey = TournamentExtraPropertyKey.SENBATSU_CHALLENGE_DISTANCE;
    this.tournamentExtendedPropertiesService.update(tournamentProperty).subscribe((): void => {
      this.messageService.infoMessage('infoTournamentUpdated');
    });
  }

  closeDialog(): void {
    this.dialogRef.close();
  }

  fifoToggle($event: MatSlideToggleChange): void {
    const tournamentProperty: TournamentExtendedProperty = new TournamentExtendedProperty();
    tournamentProperty.tournament = this.tournament;
    tournamentProperty.propertyValue = $event.checked ? LeagueFightsOrder.FIFO : LeagueFightsOrder.LIFO;
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

  avoidDuplicatesToggle($event: MatSlideToggleChange): void {
    const tournamentProperty: TournamentExtendedProperty = new TournamentExtendedProperty();
    tournamentProperty.tournament = this.tournament;
    tournamentProperty.propertyValue = $event.checked + "";
    tournamentProperty.propertyKey = TournamentExtraPropertyKey.AVOID_DUPLICATES;
    this.tournamentExtendedPropertiesService.update(tournamentProperty).subscribe((): void => {
      this.messageService.infoMessage('infoTournamentUpdated');
    });
  }

  resolveOddFightsAsapToggle($event: MatSlideToggleChange): void {
    const tournamentProperty: TournamentExtendedProperty = new TournamentExtendedProperty();
    tournamentProperty.tournament = this.tournament;
    tournamentProperty.propertyValue = $event.checked + "";
    tournamentProperty.propertyKey = TournamentExtraPropertyKey.ODD_FIGHTS_RESOLVED_ASAP;
    this.tournamentExtendedPropertiesService.update(tournamentProperty).subscribe((): void => {
      this.messageService.infoMessage('infoTournamentUpdated');
    });
  }
}
