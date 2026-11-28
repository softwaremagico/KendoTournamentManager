import {RoleType} from '../../models/role-type';

/** Public information needed to render the credential-free registration form. */
export interface RegistrationTeam {
  name: string;
  freeSlots: number;
}

export interface RegistrationClub {
  id: number;
  name: string;
  city?: string;
  country?: string;
  teams?: RegistrationTeam[];
}

export interface RegistrationConfiguration {
  tournamentName: string;
  teamSize?: number;
  teamSelectionEnabled: boolean;
  clubs: RegistrationClub[];
  availableRoles: RoleType[];
}

/** Fields sent to the public registration endpoint (no credentials are included). */
export interface RegistrationRequest {
  clubId?: number;
  newClubName?: string;
  newClubCity?: string;
  newClubCountry?: string;
  idCard: string;
  name: string;
  lastname: string;
  roles: RoleType[];
  teamPreference?: string;
}

export interface RegistrationResponse {
  id?: number;
  participantId?: number;
  name: string;
  lastname: string;
  roles: RoleType[];
  teamName?: string;
  existingParticipant: boolean;
  pendingReview: boolean;
}
