import {NgModule} from '@angular/core';
import {CommonModule} from '@angular/common';
import {FormsModule} from '@angular/forms';
import {TranslocoModule} from '@jsverse/transloco';
import {BiitButtonModule} from '@biit-solutions/wizardry-theme/button';
import {
  BiitCheckboxModule,
  BiitDropdownModule,
  BiitInputTextModule,
  BiitRadioButtonModule
} from '@biit-solutions/wizardry-theme/inputs';
import {DropdownInterfacePipeModule} from '../../pipes/dropdown-interface-pipe/dropdown-interface-pipe.module';
import {RegistrationComponent} from './registration.component';
import {RegistrationRoutingModule} from './registration-routing.module';

@NgModule({
  declarations: [RegistrationComponent],
  imports: [
    CommonModule,
    FormsModule,
    TranslocoModule,
    BiitButtonModule,
    BiitCheckboxModule,
    BiitDropdownModule,
    BiitInputTextModule,
    BiitRadioButtonModule,
    DropdownInterfacePipeModule,
    RegistrationRoutingModule
  ]
})
export class RegistrationModule {
}
