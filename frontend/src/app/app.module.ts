import {ErrorHandler, isDevMode, NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {MatSliderModule} from '@angular/material/slider';
import {MatButtonModule} from '@angular/material/button';
import {MatListModule} from '@angular/material/list';
import {MatSidenavModule} from '@angular/material/sidenav';
import {MatIconModule} from '@angular/material/icon';
import {MatToolbarModule} from '@angular/material/toolbar';
import {HTTP_INTERCEPTORS, HttpClientModule} from '@angular/common/http';
import {AppComponent} from './app.component';
import {AppRoutingModule} from './app-routing.module';
import {ClubListComponent} from './views/club-list/club-list.component';
import {MatTableModule} from "@angular/material/table";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatMenuModule} from "@angular/material/menu";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatDialogModule} from "@angular/material/dialog";
import {MatSortModule} from '@angular/material/sort';
import {MatInputModule} from "@angular/material/input";
import {MatSelectModule} from "@angular/material/select";
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {MatCardModule} from "@angular/material/card";
import {MatExpansionModule} from "@angular/material/expansion";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {BasicTableModule} from "./components/basic/basic-table/basic-table.module";
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {DragDropModule} from "@angular/cdk/drag-drop";
import {IconModule} from "./components/icons";
import {registerLocaleData} from "@angular/common";
import localeES from "@angular/common/locales/es";
import localeCAT from "@angular/common/locales/ca-ES-valencia";
import localeIT from "@angular/common/locales/it";
import localeDE from "@angular/common/locales/de";
import localeNL from "@angular/common/locales/nds-NL";
import {MatTabsModule} from "@angular/material/tabs";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {AuthenticatedUserListComponent} from './views/authenticated-user-list/authenticated-user-list.component';
import {PasswordsComponent} from './views/passwords/passwords.component';
import {MatTooltipModule} from "@angular/material/tooltip";
import {RbacModule} from "./pipes/rbac-pipe/rbac.module";
import {DrawModule} from "./components/fight/duel/draw/draw.module";
import {UserScoreModule} from "./components/fight/duel/user-score/user-score.module";
import {DuelModule} from "./components/fight/duel/duel.module";
import {MatSpinnerOverlayModule} from "./components/mat-spinner-overlay/mat-spinner-overlay.module";
import {FightModule} from "./components/fight/fight.module";
import {UntieFightModule} from "./components/untie-fight/untie-fight.module";
import {TimerModule} from "./components/timer/timer.module";
import {CompetitorsRankingModule} from "./components/competitors-ranking/competitors-ranking.module";
import {FightCreatorModule} from "./components/fight-creator/fight-creator.module";
import {LeagueGeneratorModule} from "./views/fight-list/league-generator/league-generator.module";
import {TeamRankingModule} from "./components/team-ranking/team-ranking.module";
import {UndrawTeamsModule} from "./components/undraw-teams/undraw-teams.module";
import {TournamentTeamsModule} from "./views/tournament-list/tournament-teams/tournament-teams.module";
import {TournamentRolesModule} from "./views/tournament-list/tournament-roles/tournament-roles.module";
import {FightListModule} from "./views/fight-list/fight-list-module";
import {FightStatisticsPanelModule} from "./components/fight-statistics-panel/fight-statistics-panel.module";
import {MatSlideToggleModule} from "@angular/material/slide-toggle";
import {WebcamModule} from "ngx-webcam";
import {
  ParticipantPictureDialogModule
} from "./views/participant-list/participant-form-popup/participant-picture/participant-picture-dialog-box.module";
import {ParticipantPictureModule} from "./components/participant-picture/participant-picture.module";
import {PictureDialogBoxModule} from "./components/participant-picture/picture-dialog-box/picture-dialog-box.module";
import {RoleSelectorModule} from "./components/role-selector/role-selector.module";
import {AchievementTileModule} from "./components/achievement-tile/achievement-tile.module";
import {AchievementWallModule} from "./components/achievement-wall/achievement-wall.module";
import {BarChartModule} from "./components/charts/bar-chart/bar-chart.module";
import {TournamentStatisticsComponent} from './views/tournament-statistics/tournament-statistics.component';
import {PieChartModule} from "./components/charts/pie-chart/pie-chart.module";
import {StackedBarsChartModule} from "./components/charts/stacked-bars-chart/stacked-bars-chart.module";
import {LineChartModule} from "./components/charts/line-chart/line-chart.module";
import {NgApexchartsModule} from "ng-apexcharts";
import {RadarChartModule} from "./components/charts/radar-chart/radar-chart.module";
import {RadialChartModule} from "./components/charts/radial-chart/radial-chart.module";
import {GaugeChartModule} from "./components/charts/gauge-chart/gauge-chart.module";
import {ParticipantStatisticsComponent} from './views/participant-statistics/participant-statistics.component';
import {ProgressBarModule} from "./components/progress-bar/progress-bar.module";
import {HeaderInterceptor} from "./interceptors/header-interceptor";
import {HttpErrorInterceptor} from "./interceptors/http-error-interceptor";
import {TournamentListModule} from "./views/tournament-list/tournament-list.module";
import {ParticipantListModule} from "./views/participant-list/participant-list.module";
import {
  TournamentBracketsModule
} from "./components/tournament-brackets-editor/tournament-brackets/tournament-brackets.module";
import {ShiaijoModule} from "./components/tournament-brackets-editor/tournament-brackets/shiaijo/shiaijo.module";
import {LocalErrorHandler} from "./interceptors/local-error-handler.service";
import {
  TournamentBracketsEditorModule
} from "./components/tournament-brackets-editor/tournament-brackets-editor.module";
import {TournamentGeneratorModule} from "./views/fight-list/tournament-generator/tournament-generator.module";
import {RxStompService} from "./websockets/rx-stomp.service";
import {rxStompServiceFactory} from "./websockets/rx-stomp-service-factory";
import {TournamentQrCodeModule} from './components/tournament-qr-code/tournament-qr-code.module';
import {ParticipantQrCodeModule} from './components/participant-qr-code/participant-qr-code.module';
import {ParticipantFightListModule} from './views/participant-fight-list/participant-fight-list.module';
import {BiitButtonModule, BiitIconButtonModule} from "@biit-solutions/wizardry-theme/button";
import {BiitCookiesConsentModule, BiitProgressBarModule, BiitSnackbarModule} from "@biit-solutions/wizardry-theme/info";
import {NavbarModule} from "./components/navigation/navbar/navbar.module";
import {TRANSLOCO_CONFIG, translocoConfig, TranslocoModule} from "@ngneat/transloco";
import {HasPermissionPipe} from "./pipes/has-permission.pipe";
import {BiitDatatableModule} from "@biit-solutions/wizardry-theme/table";
import {BiitPopupModule} from "@biit-solutions/wizardry-theme/popup";
import {TournamentFormPopupModule} from "./views/tournament-list/tournament-form-popup/tournament-form-popup.module";
import {
  AuthenticatedUserFormPopupModule
} from "./views/authenticated-user-list/authenticated-user-form-popup/authenticated-user-form-popup.module";
import {ClubFormPopupModule} from "./views/club-list/club-form-popup/club-form-popup.module";
import {RedirectGuard} from "./components/navigation/redirect-guard/redirect.guard";
import {KeyReversePipe} from "./pipes/keyReverse.pipe";


registerLocaleData(localeES, "es");
registerLocaleData(localeIT, "it");
registerLocaleData(localeCAT, "ca");
registerLocaleData(localeDE, "de");
registerLocaleData(localeNL, "nl");

@NgModule({
  declarations: [
    AppComponent,
    ClubListComponent,
    AuthenticatedUserListComponent,
    PasswordsComponent,
    TournamentStatisticsComponent,
    ParticipantStatisticsComponent
  ],
  imports: [
    NavbarModule,
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    MatToolbarModule,
    MatSidenavModule,
    MatListModule,
    MatButtonModule,
    MatIconModule,
    MatSliderModule,
    AppRoutingModule,
    MatTableModule,
    MatPaginatorModule,
    MatMenuModule,
    FormsModule,
    MatFormFieldModule,
    MatDialogModule,
    MatSortModule,
    MatInputModule,
    MatSelectModule,
    MatSnackBarModule,
    MatCardModule,
    FormsModule,
    ReactiveFormsModule,
    MatExpansionModule,
    MatCheckboxModule,
    BasicTableModule,
    MatAutocompleteModule,
    DragDropModule,
    IconModule,
    MatTabsModule,
    MatProgressSpinnerModule,
    MatTooltipModule,
    RbacModule,
    DrawModule,
    UserScoreModule,
    DuelModule,
    MatSpinnerOverlayModule,
    FightModule,
    UntieFightModule,
    TimerModule,
    CompetitorsRankingModule,
    FightCreatorModule,
    LeagueGeneratorModule,
    TeamRankingModule,
    ParticipantPictureDialogModule,
    TournamentTeamsModule,
    TournamentRolesModule,
    UndrawTeamsModule,
    FightListModule,
    FightStatisticsPanelModule,
    MatSlideToggleModule,
    WebcamModule,
    ParticipantPictureModule,
    PictureDialogBoxModule,
    RoleSelectorModule,
    AchievementTileModule,
    AchievementWallModule,
    BarChartModule,
    PieChartModule,
    StackedBarsChartModule,
    LineChartModule,
    RadarChartModule,
    NgApexchartsModule,
    RadialChartModule,
    GaugeChartModule,
    ProgressBarModule,
    TournamentListModule,
    ParticipantListModule,
    ProgressBarModule,
    TournamentBracketsModule,
    ShiaijoModule,
    TournamentBracketsEditorModule,
    TournamentGeneratorModule,
    TournamentQrCodeModule,
    ParticipantQrCodeModule,
    ParticipantFightListModule,
    BiitButtonModule,
    BiitProgressBarModule,
    BiitSnackbarModule,
    TranslocoModule,
    BiitCookiesConsentModule,
    HasPermissionPipe,
    KeyReversePipe,
    BiitDatatableModule,
    BiitIconButtonModule,
    BiitPopupModule,
    TournamentFormPopupModule,
    AuthenticatedUserFormPopupModule,
    ClubFormPopupModule
  ],
  providers: [
    {
      provide: ErrorHandler,
      useClass: LocalErrorHandler
    },
    {
      provide: HTTP_INTERCEPTORS,
      useClass: HeaderInterceptor,
      multi: true
    }, {
      provide: HTTP_INTERCEPTORS,
      useClass: HttpErrorInterceptor,
      multi: true
    }, {
      provide: RxStompService,
      useFactory: rxStompServiceFactory,
    }, {
      provide: TRANSLOCO_CONFIG,
      useValue: translocoConfig({
        availableLangs: ['en', 'es', 'ca', 'it', 'nl', 'de'],
        defaultLang: 'en',
        fallbackLang: 'en',
        missingHandler: {
          useFallbackTranslation: true
        },
        reRenderOnLangChange: true,
        prodMode: !isDevMode()
      })
    },
    RedirectGuard],
  bootstrap: [AppComponent]
})
export class AppModule {
}
