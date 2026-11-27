import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {TranslocoModule} from '@jsverse/transloco';
import {BiitButtonModule} from '@biit-solutions/wizardry-theme/button';
import {RegistrationComponent} from './registration.component';
import {RegistrationRoutingModule} from './registration-routing.module';

@NgModule({
  declarations: [RegistrationComponent],
  imports: [
    CommonModule,
    FormsModule,
    TranslocoModule,
    BiitButtonModule,
    RegistrationRoutingModule
  ]
})
export class RegistrationModule {
}
