import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {DrawResolution} from "../../models/draw-resolution";
import {SwissTieBreakRule} from "../../models/swiss-tie-break-rule";
import {Type} from "@biit-solutions/wizardry-theme/inputs";
import {RbacBasedComponent} from "../../components/RbacBasedComponent";
import {RbacService} from "../../services/rbac/rbac.service";
import {TranslocoService} from "@jsverse/transloco";
import {TournamentExtendedPropertiesService} from "../../services/tournament-extended-properties.service";
import {MessageService} from "../../services/message.service";
import {TournamentExtendedProperty} from "../../models/tournament-extended-property.model";
import {TournamentExtraPropertyKey} from "../../models/tournament-extra-property-key";
import {LeagueFightsOrder} from "../../models/league-fights-order";
import {Tournament} from "../../models/tournament";
import {TournamentType} from "../../models/tournament-type";
import {combineLatest, takeUntil} from "rxjs";

@Component({
  standalone: false,
  selector: 'tournament-extra-properties-form',
  templateUrl: './tournament-extra-properties-form.component.html',
  styleUrls: ['./tournament-extra-properties-form.component.scss']
})
export class TournamentExtraPropertiesFormComponent extends RbacBasedComponent implements OnInit {

  @Input()
  tournament: Tournament;
  @Output()
  closed: EventEmitter<void> = new EventEmitter<void>();

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

  // Swiss
  canConfigureSwiss: boolean;
  swissRounds: number | null;
  swissTieBreakRule: SwissTieBreakRule;
  swissAvoidRepeatedPairings: boolean;
  protected swissTieBreakRules = SwissTieBreakRule.toArray();
  protected swissTieBreakRuleValues: { value: string, label: string, description: string }[] = [];
  protected readonly TournamentExtraPropertyKey = TournamentExtraPropertyKey;
  protected readonly Type = Type;

  constructor(rbacService: RbacService, public transloco: TranslocoService,
              private readonly tournamentExtendedPropertiesService: TournamentExtendedPropertiesService,
              public messageService: MessageService) {
    super(rbacService);

    this.drawResolution = DrawResolution.toArray();
    this.defaultPropertiesValue();
    this.translateSenbatsuChallengeDistance();
    this.translateDrawResolution();
    this.translateSwissTieBreakRules();
  }

  ngOnInit(): void {
    this.canMaximizeFights = TournamentType.canMaximizeFights(this.tournament.type);
    this.needsDrawResolution = TournamentType.needsDrawResolution(this.tournament.type);
    this.canSelectChallengeDistance = TournamentType.canSelectChallengeDistance(this.tournament.type);
    this.needsFifoWinner = TournamentType.needsFifoWinner(this.tournament.type);
    this.canAvoidDuplicatedFights = TournamentType.avoidsDuplicatedFights(this.tournament.type);
    this.canResolveOddFightsAsap = TournamentType.resolveOddFightsAsap(this.tournament.type)
    this.canConfigureSwiss = TournamentType.isSwiss(this.tournament.type);

    this.tournamentExtendedPropertiesService.getByTournament(this.tournament).pipe(takeUntil(this.destroySubject)).subscribe((_tournamentSelection: TournamentExtendedProperty[]): void => {
      if (_tournamentSelection) {
        for (const _tournamentProperty of _tournamentSelection) {
          this.applyTournamentProperty(_tournamentProperty);
        }
      }
    });
  }

  private applyTournamentProperty(property: TournamentExtendedProperty): void {
    switch (property.propertyKey) {
      case TournamentExtraPropertyKey.KING_DRAW_RESOLUTION:
        this.selectedDrawResolution = DrawResolution.getByKey(property.propertyValue);
        break;
      case TournamentExtraPropertyKey.MAXIMIZE_FIGHTS:
        this.areFightsMaximized = property.propertyValue.toLowerCase() === "true";
        break;
      case TournamentExtraPropertyKey.LEAGUE_FIGHTS_ORDER_GENERATION:
        this.firstInFirstOut = property.propertyValue.toUpperCase() === LeagueFightsOrder.FIFO;
        break;
      case TournamentExtraPropertyKey.AVOID_DUPLICATES:
        this.avoidDuplicatedFights = property.propertyValue.toLowerCase() === "true";
        break;
      case TournamentExtraPropertyKey.ODD_FIGHTS_RESOLVED_ASAP:
        this.resolveOddFightsAsap = property.propertyValue.toLowerCase() === "true";
        break;
      case TournamentExtraPropertyKey.SENBATSU_CHALLENGE_DISTANCE:
        this.challengeDistance = Number.isNaN(+property.propertyValue)
          ? TournamentExtraPropertyKey.senbatsuChallengeDistance()
          : Number(property.propertyValue);
        break;
      case TournamentExtraPropertyKey.SWISS_ROUNDS: {
        const parsed: number = Number(property.propertyValue);
        this.swissRounds = Number.isNaN(parsed) ? null : parsed;
        break;
      }
      case TournamentExtraPropertyKey.SWISS_TIE_BREAK_RULE:
        this.swissTieBreakRule = SwissTieBreakRule.getByKey(property.propertyValue)
          ?? TournamentExtraPropertyKey.swissDefaultTieBreakRule();
        break;
      case TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS:
        this.swissAvoidRepeatedPairings = property.propertyValue.toLowerCase() === 'true';
        break;
      default:
        break;
    }
  }

  defaultPropertiesValue(): void {
    this.areFightsMaximized = TournamentExtraPropertyKey.getDefaultMaximizedFights();
    this.selectedDrawResolution = TournamentExtraPropertyKey.getDefaultKingDrawResolutions();
    this.firstInFirstOut = TournamentExtraPropertyKey.getDefaultLeagueFightsOrderGeneration();
    this.avoidDuplicatedFights = TournamentExtraPropertyKey.avoidDuplicateFightsGeneration();
    this.resolveOddFightsAsap = TournamentExtraPropertyKey.oddFightsResolvedAsap();
    this.challengeDistance = TournamentExtraPropertyKey.senbatsuChallengeDistance();
    this.swissRounds = TournamentExtraPropertyKey.swissDefaultRounds();
    this.swissTieBreakRule = TournamentExtraPropertyKey.swissDefaultTieBreakRule();
    this.swissAvoidRepeatedPairings = TournamentExtraPropertyKey.swissDefaultAvoidRepeatedPairings();
  }

  private translateDrawResolution() {
    const scoresTranslations = this.drawResolutions.map(drawValue => this.transloco.selectTranslate(`${DrawResolution.toCamel(drawValue)}`));
    combineLatest(scoresTranslations).pipe(takeUntil(this.destroySubject)).subscribe((translations) => {
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

  private translateSwissTieBreakRules(): void {
    const translations = this.swissTieBreakRules.map(rule =>
      this.transloco.selectTranslate(SwissTieBreakRule.toCamel(rule))
    );
    combineLatest(translations).pipe(takeUntil(this.destroySubject)).subscribe((labels) => {
      labels.forEach((label, index) => this.swissTieBreakRuleValues.push({
        value: this.swissTieBreakRules[index],
        label: label,
        description: this.transloco.translate(SwissTieBreakRule.toCamel(this.swissTieBreakRules[index]) + 'Hint')
      }));
    });
  }

  onSave(tournamentExtraProperty: TournamentExtraPropertyKey, propertyValue: string) {
    if (tournamentExtraProperty === TournamentExtraPropertyKey.LEAGUE_FIGHTS_ORDER_GENERATION) {
      if (propertyValue === "true") {
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
