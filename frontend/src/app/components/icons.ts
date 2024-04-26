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
      .addSvgIcon("unfinish", this.setPath(`${this.path}/unfinish.svg`))
      .addSvgIcon("clone", this.setPath(`${this.path}/clone.svg`))
      .addSvgIcon("one-winner", this.setPath(`${this.path}/one-winner.svg`))
      .addSvgIcon("two-winners", this.setPath(`${this.path}/two-winners.svg`))
      .addSvgIcon("zip-file", this.setPath(`${this.path}/zip-file.svg`))
      .addSvgIcon("sorted", this.setPath(`${this.path}/sorted.svg`));
  }

  private setPath(url: string): SafeResourceUrl {
    return this.domSanitizer.bypassSecurityTrustResourceUrl(url);
  }
}
