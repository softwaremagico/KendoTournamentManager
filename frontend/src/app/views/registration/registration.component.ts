import {ChangeDetectionStrategy, Component, OnInit} from '@angular/core';
import {ActivatedRoute} from '@angular/router';
import {RoleType} from '../../models/role-type';
import {RegistrationService} from '../../services/registration.service';
import {
  RegistrationConfiguration,
  RegistrationRequest,
  RegistrationResponse
} from '../../services/models/registration.model';

@Component({
  changeDetection: ChangeDetectionStrategy.Eager,
  standalone: false,
  selector: 'app-registration',
  templateUrl: './registration.component.html',
  styleUrls: ['./registration.component.scss']
})
export class RegistrationComponent implements OnInit {

  invitation: string = '';
  configuration: RegistrationConfiguration | undefined;
  loading: boolean = true;
  submitting: boolean = false;
  registered: RegistrationResponse | undefined;
  errorMessage: string | undefined;

  useExistingClub: boolean = true;
  selectedClubId: number | undefined;
  request: RegistrationRequest = {
    idCard: '', name: '', lastname: '', roles: []
  };

  constructor(private readonly activatedRoute: ActivatedRoute, private readonly registrationService: RegistrationService) {
  }

  ngOnInit(): void {
    this.activatedRoute.queryParams.subscribe(params => {
      const invitation: string = params['invitation'];
      if (!invitation) {
        this.loading = false;
        this.errorMessage = 'registrationInvalidInvitation';
        return;
      }
      this.invitation = invitation;
      this.loadConfiguration();
    });
  }

  private loadConfiguration(): void {
    this.loading = true;
    this.registrationService.configuration(this.invitation).subscribe(configuration => {
      this.loading = false;
      if (configuration) {
        this.configuration = configuration;
      } else {
        this.errorMessage = 'registrationError';
      }
    });
  }

  roleKey(role: RoleType): string {
    return role.toLowerCase();
  }

  isRoleSelected(role: RoleType): boolean {
    return this.request.roles.includes(role);
  }

  selectRole(role: RoleType): void {
    if (!this.request.roles.includes(role)) {
      this.request.roles.push(role);
    }
  }

  deselectRole(role: RoleType): void {
    this.request.roles = this.request.roles.filter(selected => selected !== role);
  }

  isValid(): boolean {
    const hasClub: boolean = this.useExistingClub ? this.selectedClubId !== undefined
      : !!(this.request.newClubName?.trim() && this.request.newClubCity?.trim() && this.request.newClubCountry?.trim());
    return !!(this.request.name?.trim() && this.request.lastname?.trim() && this.request.idCard?.trim()
      && this.request.roles.length > 0 && hasClub);
  }

  submit(): void {
    if (!this.isValid() || this.submitting) {
      return;
    }
    this.submitting = true;
    this.errorMessage = undefined;
    const request: RegistrationRequest = {
      idCard: this.request.idCard.trim(),
      name: this.request.name.trim(),
      lastname: this.request.lastname.trim(),
      roles: [...this.request.roles],
      clubId: this.useExistingClub ? this.selectedClubId : undefined,
      newClubName: !this.useExistingClub ? this.request.newClubName?.trim() : undefined,
      newClubCity: !this.useExistingClub ? this.request.newClubCity?.trim() : undefined,
      newClubCountry: !this.useExistingClub ? this.request.newClubCountry?.trim() : undefined,
      teamPreference: this.configuration?.teamSelectionEnabled ? this.request.teamPreference?.trim() : undefined
    };
    this.registrationService.register(this.invitation, request).subscribe(response => {
      this.submitting = false;
      if (response) {
        this.registered = response;
      } else {
        this.errorMessage = 'registrationSubmitError';
      }
    });
  }
}
