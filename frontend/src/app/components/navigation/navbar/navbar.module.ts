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
import {MatIconModule} from "@angular/material/icon";
import {BiitButtonModule} from "@biit-solutions/wizardry-theme/button";
import {BiitPopupModule} from "@biit-solutions/wizardry-theme/popup";

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
        MatIconModule,
        BiitButtonModule,
        BiitPopupModule,
    ],
  exports: [NavbarComponent],
})
export class NavbarModule {
}
