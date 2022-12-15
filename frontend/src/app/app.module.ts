import {NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {MatSliderModule} from '@angular/material/slider';
import {MatButtonModule} from '@angular/material/button';
import {MatListModule} from '@angular/material/list';
import {MatSidenavModule} from '@angular/material/sidenav';
import {MatIconModule} from '@angular/material/icon';
import {MatToolbarModule} from '@angular/material/toolbar';
import {HttpClient, HttpClientModule} from '@angular/common/http';
import {FlexLayoutModule} from '@angular/flex-layout';
import {TranslateLoader, TranslateModule, TranslateService} from '@ngx-translate/core';
import {TranslateHttpLoader} from '@ngx-translate/http-loader';

import {AppComponent} from './app.component';
import {AppRoutingModule} from './app-routing.module';
import {ClubListComponent} from './views/club-list/club-list.component';
import {MatTableModule} from "@angular/material/table";
import {MatPaginatorIntl, MatPaginatorModule} from "@angular/material/paginator";
import {MatMenuModule} from "@angular/material/menu";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {MatFormFieldModule} from "@angular/material/form-field";
import {MatDialogModule} from "@angular/material/dialog";
import {MatSortModule} from '@angular/material/sort';
import {MatInputModule} from "@angular/material/input";
import {LoginComponent} from "./views/login/login.component";
import {CookieService} from 'ngx-cookie-service';
import {MatSelectModule} from "@angular/material/select";
import {MatSnackBarModule} from '@angular/material/snack-bar';
import {MatCardModule} from "@angular/material/card";
import {MatExpansionModule} from "@angular/material/expansion";
import {MatCheckboxModule} from "@angular/material/checkbox";
import {BasicTableModule} from "./components/basic/basic-table/basic-table.module";
import {ParticipantListComponent} from './views/participant-list/participant-list.component';
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {TournamentListComponent} from './views/tournament-list/tournament-list.component';
import {DragDropModule} from "@angular/cdk/drag-drop";
import {IconModule} from "./components/icons";
import {registerLocaleData} from "@angular/common";
import localeES from "@angular/common/locales/es";
import localeCAT from "@angular/common/locales/ca-ES-VALENCIA";
import localeIT from "@angular/common/locales/it";
import localeDE from "@angular/common/locales/de";
import localeNL from "@angular/common/locales/nds-NL";
import {MatTabsModule} from "@angular/material/tabs";
import {MatProgressSpinnerModule} from "@angular/material/progress-spinner";
import {PaginatorI18n} from "./components/basic/basic-table/paginator-i18n";
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
import {ConfirmationDialogModule} from "./components/basic/confirmation-dialog/confirmation-dialog.module";
import {
  AuthenticatedUserDialogBoxModule
} from "./views/authenticated-user-list/authenticated-user-dialog-box/authenticated-user-dialog-box.module";
import {ClubDialogBoxModule} from "./views/club-list/club-dialog-box/club-dialog-box.module";
import {CompetitorsRankingModule} from "./views/fight-list/competitors-ranking/competitors-ranking.module";
import {FightDialogBoxModule} from "./views/fight-list/fight-dialog-box/fight-dialog-box.module";
import {LeagueGeneratorModule} from "./views/fight-list/league-generator/league-generator.module";
import {TeamRankingModule} from "./views/fight-list/team-ranking/team-ranking.module";
import {UndrawTeamsModule} from "./views/fight-list/undraw-teams/undraw-teams.module";
import {
  ParticipantDialogBoxModule
} from "./views/participant-list/participant-dialog-box/participant-dialog-box.module";
import {TournamentTeamsModule} from "./views/tournament-list/tournament-teams/tournament-teams.module";
import {TournamentRolesModule} from "./views/tournament-list/tournament-roles/tournament-roles.module";
import {TournamentDialogBoxModule} from "./views/tournament-list/tournament-dialog-box/tournament-dialog-box.module";
import {FightListModule} from "./views/fight-list/fight-list-module";
import {FightStatisticsPanelModule} from "./components/fight-statistics-panel/fight-statistics-panel.module";
import {MatSlideToggleModule} from "@angular/material/slide-toggle";


registerLocaleData(localeES, "es");
registerLocaleData(localeIT, "it");
registerLocaleData(localeCAT, "ca");
registerLocaleData(localeDE, "de");
registerLocaleData(localeNL, "nl");

@NgModule({
  declarations: [
    AppComponent,
    ClubListComponent,
    LoginComponent,
    ParticipantListComponent,
    TournamentListComponent,
    AuthenticatedUserListComponent,
    PasswordsComponent
  ],
    imports: [
        BrowserModule,
        BrowserAnimationsModule,
        FlexLayoutModule,
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
        TranslateModule.forRoot({
            loader: {
                provide: TranslateLoader,
                useFactory: httpTranslateLoader,
                deps: [HttpClient]
            }
        }),
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
        ConfirmationDialogModule,
        AuthenticatedUserDialogBoxModule,
        ClubDialogBoxModule,
        CompetitorsRankingModule,
        FightDialogBoxModule,
        LeagueGeneratorModule,
        TeamRankingModule,
        ParticipantDialogBoxModule,
        TournamentTeamsModule,
        TournamentRolesModule,
        TournamentDialogBoxModule,
        UndrawTeamsModule,
        FightListModule,
        FightStatisticsPanelModule,
        MatSlideToggleModule
    ],
  providers: [CookieService, {
    provide: MatPaginatorIntl,
    useFactory: (translate: TranslateService) => {
      const service = new PaginatorI18n();
      service.injectTranslateService(translate);
      return service;
    },
    deps: [TranslateService]
  }],
  bootstrap: [AppComponent]
})
export class AppModule {
}

export function httpTranslateLoader(http: HttpClient) {
  return new TranslateHttpLoader(http);
}
