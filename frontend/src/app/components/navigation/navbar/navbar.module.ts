import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {NavbarComponent} from './navbar.component';
import {BiitIconModule} from '@biit-solutions/wizardry-theme/icon';
import {BiitNavMenuModule, BiitNavUserModule} from '@biit-solutions/wizardry-theme/navigation';
import {FormsModule} from "@angular/forms";
import {TranslocoRootModule} from "@biit-solutions/wizardry-theme/i18n";
import {ContextMenuModule} from "@perfectmemory/ngx-contextmenu";
import {ComponentMenuModule} from "../component-menu/component-menu.module";
import {HasPermissionPipe} from "../../../pipes/has-permission.pipe";

@NgModule({
  declarations: [NavbarComponent],
  imports: [
    CommonModule,
    FormsModule,
    BiitIconModule,
    BiitNavMenuModule,
    BiitNavUserModule,
    TranslocoRootModule,
    ContextMenuModule,
    ComponentMenuModule,
    HasPermissionPipe,
  ],
  exports: [NavbarComponent],
})
export class NavbarModule {
}
