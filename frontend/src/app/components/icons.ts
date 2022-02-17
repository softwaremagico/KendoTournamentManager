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
      .addSvgIcon("fight", this.setPath(`${this.path}/fight.svg`));
  }

  private setPath(url: string): SafeResourceUrl {
    return this.domSanitizer.bypassSecurityTrustResourceUrl(url);
  }
}
