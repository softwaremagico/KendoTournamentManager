import {Component, ElementRef, OnInit, ViewChild} from '@angular/core';
import {Route, Router} from '@angular/router';
import {provideTranslocoScope, TranslocoService} from '@ngneat/transloco';
import {ContextMenuComponent, ContextMenuService} from "@perfectmemory/ngx-contextmenu";
import {AuthenticatedUser} from "../../../models/authenticated-user";
import {UserSessionService} from "../../../services/user-session.service";
import {Constants} from "../../../constants";
import {AuthGuard} from "../../../services/auth-guard.service";
import {ActivityService} from "../../../services/rbac/activity.service";
import {RbacActivity} from "../../../services/rbac/rbac.activity";
import {ClubListComponent} from "../../../views/club-list/club-list.component";
import {ParticipantListComponent} from "../../../views/participant-list/participant-list.component";
import {UserListComponent} from "../../basic/user-list/user-list.component";
import {RbacService} from "../../../services/rbac/rbac.service";

@Component({
  selector: 'navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss'],
  providers: [provideTranslocoScope({scope: 'navigation', alias: 't'})]
})

export class NavbarComponent implements OnInit {
  protected readonly Constants = Constants;

  @ViewChild('contextMenu') contextMenu: ContextMenuComponent<void>;
  @ViewChild('navUser', {read: ElementRef}) navUser: ElementRef;
  user: AuthenticatedUser | undefined;

  protected readonly RbacActivity = RbacActivity;
  protected logoutConfirmation: boolean = false;
  protected languagePopup: boolean = false;
  protected passwordPopup: boolean = false;

  constructor(protected router: Router,
              private contextMenuService: ContextMenuService<void>,
              private translocoService: TranslocoService,
              protected sessionService: UserSessionService,
              private activityService: ActivityService,
              protected rbacService: RbacService) {
  }

  routes: Route[] = [];

  ngOnInit() {
    this.user = this.sessionService.getUser();
    this.routes = [
      {
        path: Constants.PATHS.REGISTRY.ROOT,
        canActivate: [AuthGuard],
        title: 'registry',
        data: {
          hidden: !this.activityService.isAllowed(RbacActivity.REGISTER_ELEMENTS)
        },
        children: [
          {
            path: Constants.PATHS.REGISTRY.CLUBS,
            component: ClubListComponent,
            canActivate: [AuthGuard],
            title: 'clubs',
            data: {
              hidden: !this.activityService.isAllowed(RbacActivity.READ_ALL_CLUBS)
            }
          },
          {
            path: Constants.PATHS.REGISTRY.PARTICIPANTS,
            component: ParticipantListComponent,
            canActivate: [AuthGuard],
            title: 'participants',
            data: {
              hidden: !this.activityService.isAllowed(RbacActivity.READ_ALL_PARTICIPANTS)
            }
          }]
      },
      {
        path: Constants.PATHS.TOURNAMENTS.ROOT,
        canActivate: [AuthGuard],
        title: 'competitions',
        data: {
          hidden: !this.activityService.isAllowed(RbacActivity.READ_ALL_TOURNAMENTS)
        }
      },
      {
        path: Constants.PATHS.ADMINISTRATION.ROOT,
        canActivate: [AuthGuard],
        title: 'administration',
        data: {
          hidden: !this.activityService.isAllowed(RbacActivity.READ_ALL_USERS)
        },
        children: [
          {
            path: Constants.PATHS.ADMINISTRATION.USERS,
            component: UserListComponent,
            canActivate: [AuthGuard],
            title: 'users',
            data: {
              hidden: !this.activityService.isAllowed(RbacActivity.READ_ALL_USERS)
            }
          }]
      },
      {
        path: Constants.PATHS.HELP.ROOT,
        title: 'help',
        children: [
          {
            path: Constants.PATHS.HELP.WIKI,
            title: 'wiki',
          },
          {
            path: Constants.PATHS.HELP.LICENSE,
            title: 'licenses',
          },
          {
            path: Constants.PATHS.HELP.ABOUT,
            title: 'about',
          }]
      },
    ]
    this.routes.forEach(route => {
      this.translocoService.selectTranslate(route.title as string, {}, {scope: 'navigation'}).subscribe(value => route.title = value);

      route.children?.forEach(child => {
        this.translocoService.selectTranslate(child.title as string, {}, {scope: 'navigation'}).subscribe(value => child.title = value);
      })
    });
  }

  protected onContextMenu($event: Event): void {
    this.contextMenuService.show(
      this.contextMenu,
      {
        x: this.navUser.nativeElement.offsetLeft + this.navUser.nativeElement.offsetWidth,
        y: this.navUser.nativeElement.offsetHeight
      }
    );
    $event.preventDefault();
    $event.stopPropagation();
  }

  openWiki(): void {
    window.open("https://github.com/softwaremagico/KendoTournamentManager/wiki", "_blank");
  }

  openAbout(): void {
    window.open("https://github.com/softwaremagico/KendoTournamentManager", "_blank");
  }
}

