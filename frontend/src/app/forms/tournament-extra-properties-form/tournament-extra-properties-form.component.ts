import {Component, Input, OnInit} from '@angular/core';
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
import {combineLatest, concatMap, from, map, Observable, takeUntil, toArray} from "rxjs";

@Component({
  standalone: false,
  selector: 'tournament-extra-properties-form',
  templateUrl: './tournament-extra-properties-form.component.html',
  styleUrls: ['./tournament-extra-properties-form.component.scss']
})
export class TournamentExtraPropertiesFormComponent extends RbacBasedComponent implements OnInit {

  @Input()
  tournament: Tournament;
  propertiesLoaded = false;

  drawResolution: DrawResolution[];

  get canMaximizeFights(): boolean {
    return TournamentType.canMaximizeFights(this.tournament?.type);
  }

  get needsDrawResolution(): boolean {
    return TournamentType.needsDrawResolution(this.tournament?.type);
  }

  get canSelectChallengeDistance(): boolean {
    return TournamentType.canSelectChallengeDistance(this.tournament?.type);
  }

  get needsFifoWinner(): boolean {
    return TournamentType.needsFifoWinner(this.tournament?.type);
  }

  get canResolveOddFightsAsap(): boolean {
    return TournamentType.resolveOddFightsAsap(this.tournament?.type);
  }

  get canAvoidDuplicatedFights(): boolean {
    return TournamentType.avoidsDuplicatedFights(this.tournament?.type);
  }

  get canConfigureSwiss(): boolean {
    return TournamentType.isSwiss(this.tournament?.type);
  }

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

  swissRounds: number | null;
  swissTieBreakRule: SwissTieBreakRule;
  swissAvoidRepeatedPairings: boolean;
  private persistedPropertyKeys = new Set<TournamentExtraPropertyKey>();
  protected swissTieBreakRules = SwissTieBreakRule.toArray();
  protected swissTieBreakRuleValues: { value: string, label: string, description: string }[] = [];
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
    if (!this.tournament.id) {
      this.propertiesLoaded = true;
      return;
    }

    this.tournamentExtendedPropertiesService.getByTournament(this.tournament).pipe(takeUntil(this.destroySubject)).subscribe((_tournamentSelection: TournamentExtendedProperty[]): void => {
      if (_tournamentSelection) {
        for (const _tournamentProperty of _tournamentSelection) {
          this.applyTournamentProperty(_tournamentProperty);
        }
      }
      this.propertiesLoaded = true;
    });
  }

  private applyTournamentProperty(property: TournamentExtendedProperty): void {
    this.persistedPropertyKeys.add(property.propertyKey);
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

  saveProperties(tournament: Tournament): Observable<void> {
    const properties: TournamentExtendedProperty[] = [];
    const tournamentType = tournament.type;
    const addProperty = (key: TournamentExtraPropertyKey, value: string): void => {
      const property = new TournamentExtendedProperty(tournament, key, value);
      property.propertyValue = value;
      properties.push(property);
    };

    if (TournamentType.canMaximizeFights(tournamentType)) {
      addProperty(TournamentExtraPropertyKey.MAXIMIZE_FIGHTS, String(this.areFightsMaximized));
      addProperty(TournamentExtraPropertyKey.ODD_FIGHTS_RESOLVED_ASAP, String(this.resolveOddFightsAsap));
    }
    if (TournamentType.avoidsDuplicatedFights(tournamentType)) {
      addProperty(TournamentExtraPropertyKey.AVOID_DUPLICATES, String(this.avoidDuplicatedFights));
    }
    if (TournamentType.needsFifoWinner(tournamentType)) {
      addProperty(
        TournamentExtraPropertyKey.LEAGUE_FIGHTS_ORDER_GENERATION,
        this.firstInFirstOut ? LeagueFightsOrder.FIFO : LeagueFightsOrder.LIFO
      );
    }
    if (TournamentType.needsDrawResolution(tournamentType)) {
      addProperty(TournamentExtraPropertyKey.KING_DRAW_RESOLUTION, this.selectedDrawResolution);
    }
    if (TournamentType.canSelectChallengeDistance(tournamentType)) {
      addProperty(TournamentExtraPropertyKey.SENBATSU_CHALLENGE_DISTANCE, String(this.challengeDistance));
    }
    if (TournamentType.isSwiss(tournamentType)) {
      addProperty(TournamentExtraPropertyKey.SWISS_AVOID_REPEATED_PAIRINGS, String(this.swissAvoidRepeatedPairings));
      addProperty(TournamentExtraPropertyKey.SWISS_TIE_BREAK_RULE, this.swissTieBreakRule);
      if (this.swissRounds != null || this.persistedPropertyKeys.has(TournamentExtraPropertyKey.SWISS_ROUNDS)) {
        addProperty(TournamentExtraPropertyKey.SWISS_ROUNDS, this.swissRounds == null ? '' : String(this.swissRounds));
      }
    }

    return from(properties).pipe(
      concatMap(property => this.tournamentExtendedPropertiesService.update(property).pipe(
        map(updatedProperty => {
          if (!updatedProperty) {
            throw new Error(`Could not save tournament property ${property.propertyKey}`);
          }
          this.persistedPropertyKeys.add(property.propertyKey);
          return updatedProperty;
        })
      )),
      toArray(),
      map(() => undefined)
    );
  }
}
