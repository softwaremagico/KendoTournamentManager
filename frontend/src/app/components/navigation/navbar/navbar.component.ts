import {AfterViewInit, Component, ElementRef, ViewChild} from '@angular/core';
import {Route, Router} from '@angular/router';
import {provideTranslocoScope, TranslocoService} from '@ngneat/transloco';
import {ContextMenuComponent, ContextMenuService} from "@perfectmemory/ngx-contextmenu";
import {AuthenticatedUser} from "../../../models/authenticated-user";
import {UserSessionService} from "../../../services/user-session.service";
import {Constants} from "../../../constants";
import {AuthGuard} from "../../../services/auth-guard.service";

@Component({
  selector: 'navbar',
  templateUrl: './navbar.component.html',
  styleUrls: ['./navbar.component.scss'],
  providers: [provideTranslocoScope({scope: 'components/navigation', alias: 't'})]
})

export class NavbarComponent implements AfterViewInit {
  protected readonly Constants = Constants;

  @ViewChild('contextMenu') contextMenu: ContextMenuComponent<void>;
  @ViewChild('navUser', {read: ElementRef}) navUser: ElementRef;
  user: AuthenticatedUser | undefined;

  constructor(protected router: Router,
              private contextMenuService: ContextMenuService<void>,
              private translocoService: TranslocoService,
              protected sessionService: UserSessionService) {
  }

  routes: Route[] = [];

  ngAfterViewInit() {
    this.user = this.sessionService.getUser();
    this.routes = [
      {
        path: Constants.PATHS.TOURNAMENTS,
        canActivate: [AuthGuard],
        title: 'appointments',
      }
    ]
    this.routes.forEach(route => {
      this.translocoService.selectTranslate(route.title as string, {}, {scope: 'components/navigation'}).subscribe(value => route.title = value);

      route.children?.forEach(child => {
        this.translocoService.selectTranslate(child.title as string, {}, {scope: 'components/navigation'}).subscribe(value => child.title = value);
      })
    });

  }

  log(event: any) {
    console.log("DEVELOPMENT LOG: ", event);
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
}

