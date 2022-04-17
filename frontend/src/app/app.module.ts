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
import {TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {TranslateHttpLoader} from '@ngx-translate/http-loader';

import {AppComponent} from './app.component';
import {AppRoutingModule} from './app-routing.module';
import {ClubListComponent} from './views/club-list/club-list.component';
import {MatTableModule} from "@angular/material/table";
import {MatPaginatorModule} from "@angular/material/paginator";
import {MatMenuModule} from "@angular/material/menu";
import {ClubDialogBoxComponent} from './views/club-list/club-dialog-box/club-dialog-box.component';
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
import {
  ParticipantDialogBoxComponent
} from './views/participant-list/participant-dialog-box/participant-dialog-box.component';
import {MatAutocompleteModule} from "@angular/material/autocomplete";
import {TournamentListComponent} from './views/tournament-list/tournament-list.component';
import {
  TournamentDialogBoxComponent
} from './views/tournament-list/tournament-dialog-box/tournament-dialog-box.component';
import {UserListComponent} from './components/basic/user-list/user-list.component';
import {TournamentRolesComponent} from './views/tournament-list/tournament-roles/tournament-roles.component';
import {DragDropModule} from "@angular/cdk/drag-drop";
import {TournamentTeamsComponent} from './views/tournament-list/tournament-teams/tournament-teams.component';
import {IconModule} from "./components/icons";
import { FightListComponent } from './views/fight-list/fight-list.component';
import { FightComponent } from './fight/fight.component';
import { DuelComponent } from './fight/duel/duel.component';
import { UserScoreComponent } from './fight/duel/user-score/user-score.component';
import { ScoreComponent } from './fight/duel/user-score/score/score.component';
import { UserNameComponent } from './fight/duel/user-score/user-name/user-name.component';
import { FaultComponent } from './fight/duel/user-score/fault/fault.component';

@NgModule({
  declarations: [
    AppComponent,
    ClubListComponent,
    ClubDialogBoxComponent,
    LoginComponent,
    ParticipantListComponent,
    ParticipantDialogBoxComponent,
    TournamentListComponent,
    TournamentDialogBoxComponent,
    UserListComponent,
    TournamentRolesComponent,
    TournamentTeamsComponent,
    FightListComponent,
    FightComponent,
    DuelComponent,
    UserScoreComponent,
    ScoreComponent,
    UserNameComponent,
    FaultComponent
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
    IconModule
  ],
  providers: [CookieService],
  bootstrap: [AppComponent]
})
export class AppModule {
}

export function httpTranslateLoader(http: HttpClient) {
  return new TranslateHttpLoader(http);
}
