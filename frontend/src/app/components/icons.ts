import {NgModule} from "@angular/core";
import {DomSanitizer, SafeResourceUrl} from "@angular/platform-browser";
import {MatIconRegistry} from "@angular/material/icon";


@NgModule({})
export class IconModule {

  private path: string = "../../../assets/icons";

  constructor(private domSanitizer: DomSanitizer, public matIconRegistry: MatIconRegistry) {
    this.matIconRegistry
      .addSvgIcon("men", this.setPath(`${this.path}/men.svg`))
      .addSvgIcon("shinai", this.setPath(`${this.path}/shinai.svg`))
      .addSvgIcon("team", this.setPath(`${this.path}/team.svg`))
      .addSvgIcon("fight", this.setPath(`${this.path}/fight.svg`))
      .addSvgIcon("card", this.setPath(`${this.path}/card.svg`))
      .addSvgIcon("teams-classification", this.setPath(`${this.path}/teams-classification.svg`))
      .addSvgIcon("undraw-score", this.setPath(`${this.path}/undraw-score.svg`))
      .addSvgIcon("tournament-blog", this.setPath(`${this.path}/blog.svg`))
      .addSvgIcon("ribbon", this.setPath(`${this.path}/ribbon.svg`))
      .addSvgIcon("crown", this.setPath(`${this.path}/crown.svg`))
      .addSvgIcon("exchange-colors", this.setPath(`${this.path}/exchange.svg`))
      .addSvgIcon("exchange-teams", this.setPath(`${this.path}/exchange-team.svg`))
      .addSvgIcon("member-order", this.setPath(`${this.path}/member-order.svg`))
      .addSvgIcon("member-order-disable", this.setPath(`${this.path}/member-order-disable.svg`))
      .addSvgIcon("competitors-classification", this.setPath(`${this.path}/competitors-classification.svg`))
      .addSvgIcon("diplomas", this.setPath(`${this.path}/diploma.svg`))
      .addSvgIcon("gauge", this.setPath(`${this.path}/gauge.svg`))
      .addSvgIcon("aggressiveness", this.setPath(`${this.path}/aggressiveness.svg`))
      .addSvgIcon("affection", this.setPath(`${this.path}/affection.svg`))
      .addSvgIcon("attack", this.setPath(`${this.path}/attack.svg`))
      .addSvgIcon("defense", this.setPath(`${this.path}/defense.svg`))
      .addSvgIcon("willpower", this.setPath(`${this.path}/willpower.svg`))
      .addSvgIcon("match", this.setPath(`${this.path}/match.svg`))
      .addSvgIcon("brackets", this.setPath(`${this.path}/brackets.svg`))
      .addSvgIcon("check", this.setPath(`${this.path}/check.svg`))
      .addSvgIcon("uncheck", this.setPath(`${this.path}/uncheck.svg`))
      .addSvgIcon("clone", this.setPath(`${this.path}/clone.svg`))
      .addSvgIcon("one-winner", this.setPath(`${this.path}/one-winner.svg`))
      .addSvgIcon("two-winners", this.setPath(`${this.path}/two-winners.svg`))
      .addSvgIcon("zip-file", this.setPath(`${this.path}/zip-file.svg`))
      .addSvgIcon("yourWorstNightmare", this.setPath(`${this.path}/yourWorstNightmare.svg`))
      .addSvgIcon("youAreTheWorstNightmareOf", this.setPath(`${this.path}/youAreTheWorstNightmareOf.svg`))
      .addSvgIcon("sorted", this.setPath(`${this.path}/sorted.svg`))
      .addSvgIcon("whistle", this.setPath(`${this.path}/whistle.svg`))
      .addSvgIcon("csv-file-small", this.setPath(`${this.path}/csv-file-small.svg`))
      .addSvgIcon("bar_chart", this.setPath(`${this.path}/bar_chart.svg`))
      .addSvgIcon("badge", this.setPath(`${this.path}/badge.svg`))
      .addSvgIcon("qr_code", this.setPath(`${this.path}/qr_code.svg`))
      .addSvgIcon("arrow_back", this.setPath(`${this.path}/arrow_back.svg`))
      .addSvgIcon("wand", this.setPath(`${this.path}/wand.svg`))
      .addSvgIcon("timer", this.setPath(`${this.path}/timer.svg`))
      .addSvgIcon("timer_off", this.setPath(`${this.path}/timer_off.svg`))
      .addSvgIcon("download", this.setPath(`${this.path}/download.svg`))
      .addSvgIcon("projector", this.setPath(`${this.path}/projector.svg`))
      .addSvgIcon("menu-drag", this.setPath(`${this.path}/menu-drag.svg`))
      .addSvgIcon("play", this.setPath(`${this.path}/play.svg`))
      .addSvgIcon("pause", this.setPath(`${this.path}/pause.svg`))
      .addSvgIcon("rewind", this.setPath(`${this.path}/rewind.svg`))
      .addSvgIcon("swap_vertical", this.setPath(`${this.path}/swap_vertical.svg`))
      .addSvgIcon("arrow_forward", this.setPath(`${this.path}/arrow_forward.svg`));
  }

  private setPath(url: string): SafeResourceUrl {
    return this.domSanitizer.bypassSecurityTrustResourceUrl(url);
  }
}
