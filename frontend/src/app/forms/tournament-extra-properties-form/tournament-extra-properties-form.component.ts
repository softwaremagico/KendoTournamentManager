import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {DrawResolution} from "../../models/draw-resolution";
import {RbacBasedComponent} from "../../components/RbacBasedComponent";
import {RbacService} from "../../services/rbac/rbac.service";
import {TranslocoService} from "@ngneat/transloco";
import {TournamentExtendedPropertiesService} from "../../services/tournament-extended-properties.service";
import {MessageService} from "../../services/message.service";
import {TournamentExtendedProperty} from "../../models/tournament-extended-property.model";
import {TournamentExtraPropertyKey} from "../../models/tournament-extra-property-key";
import {LeagueFightsOrder} from "../../models/league-fights-order";
import {Tournament} from "../../models/tournament";
import {TournamentType} from "../../models/tournament-type";
import {combineLatest} from "rxjs";

@Component({
  selector: 'tournament-extra-settings-form',
  templateUrl: './tournament-extra-properties-form.component.html',
  styleUrls: ['./tournament-extra-properties-form.component.scss']
})
export class TournamentExtraPropertiesFormComponent extends RbacBasedComponent implements OnInit {

  @Input()
  tournament: Tournament;
  @Output()
  onClosed: EventEmitter<void> = new EventEmitter<void>();

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
  protected drawResolutions = DrawResolution.toArray();
  protected drawResolutionValues: { value: string, label: string, description: string }[] = [];
  avoidDuplicatedFights: boolean;
  resolveOddFightsAsap: boolean;
  challengeDistance: number;
  protected senbatsuChallengeDistance: { value: string, label: string }[] = [];
  protected SENBATSU_ALLOWED_DISTANCE: number[] = [2, 3, 4, 5];

  protected readonly TournamentExtraPropertyKey = TournamentExtraPropertyKey;

  constructor(rbacService: RbacService, public transloco: TranslocoService,
              private tournamentExtendedPropertiesService: TournamentExtendedPropertiesService,
              public messageService: MessageService) {
    super(rbacService);

    this.drawResolution = DrawResolution.toArray();
    this.defaultPropertiesValue();
    this.translateSenbatsuChallengeDistance();
    this.translateDrawResolution();
  }

  ngOnInit(): void {
    this.canMaximizeFights = TournamentType.canMaximizeFights(this.tournament.type);
    this.needsDrawResolution = TournamentType.needsDrawResolution(this.tournament.type);
    this.canSelectChallengeDistance = TournamentType.canSelectChallengeDistance(this.tournament.type);
    this.needsFifoWinner = TournamentType.needsFifoWinner(this.tournament.type);
    this.canAvoidDuplicatedFights = TournamentType.avoidsDuplicatedFights(this.tournament.type);
    this.canResolveOddFightsAsap = TournamentType.resolveOddFightsAsap(this.tournament.type)

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

  private translateDrawResolution() {
    const scoresTranslations = this.drawResolutions.map(drawValue => this.transloco.selectTranslate(`${DrawResolution.toCamel(drawValue)}`));
    combineLatest(scoresTranslations).subscribe((translations) => {
      translations.forEach((label, index) => this.drawResolutionValues.push({
        value: this.drawResolutions[index],
        label: label,
        description: this.transloco.translate(DrawResolution.toCamel(this.drawResolutions[index]) + "Hint")
      }));
    });
  }

  private translateSenbatsuChallengeDistance(): void {
    for (let number of this.SENBATSU_ALLOWED_DISTANCE) {
      this.senbatsuChallengeDistance.push({
        value: number + "", label: number + ""
      });
    }
  }

  onSave(tournamentExtraProperty: TournamentExtraPropertyKey, propertyValue: string) {
    if (tournamentExtraProperty === TournamentExtraPropertyKey.LEAGUE_FIGHTS_ORDER_GENERATION) {
      if (propertyValue == "true") {
        propertyValue = LeagueFightsOrder.FIFO;
      } else {
        propertyValue = LeagueFightsOrder.LIFO;
      }
    }
    this.tournamentExtendedPropertiesService.update(
      new TournamentExtendedProperty(this.tournament, tournamentExtraProperty, propertyValue)
    ).subscribe().add();
  }
}
