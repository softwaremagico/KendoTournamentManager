import {Component, OnInit} from "@angular/core";
import {DarkModeService} from "../../services/notifications/dark-mode.service";
import {UserSessionService} from "../../services/user-session.service";

@Component({
  template: ''
})
export abstract class CustomChartComponent implements OnInit {

  protected titleTextColor: string = "#000000"
  protected legendTextColor: string = "#000000"
  protected axisTextColor: string = "#000000"

  constructor(protected darkModeService: DarkModeService, protected userSessionService: UserSessionService) {
    this.darkModeService.darkModeSwitched.subscribe((switched: boolean) => {
      this.setFontColors(switched);
      this.setProperties();
    });
  }


  ngOnInit() {
    this.setFontColors(this.userSessionService.getNightMode());
    this.setProperties();
  }

  setFontColors(darkMode: boolean): void {
    this.titleTextColor = darkMode ? "#ffffff" : "#000000";
    this.legendTextColor = darkMode ? "#ffffff" : "#000000";
    this.axisTextColor = darkMode ? "#ffffff" : "#000000";
  }

  protected abstract setProperties(): void;
}
