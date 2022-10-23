import {Injectable} from '@angular/core';
import {UserService} from "./user.service";

@Injectable({
  providedIn: 'root'
})
export class RbacService {

  roles: string[];

  constructor(private userService: UserService) {
  }

  setRoles(roles: string[]): void {
    this.roles = roles.map(role => role.toLowerCase());
    sessionStorage.setItem("roles", JSON.stringify(roles));
  }

  getRoles(): string[] {
    if (!this.roles) {
      this.roles = JSON.parse(sessionStorage.getItem("roles")!);
      if (!this.roles) {
        this.userService.getRoles().subscribe(_roles => {
          this.setRoles(_roles);
        });
      }
    }
    return this.roles;
  }

  private hasRole(desiredRoles: string[]) {
    for (const role of desiredRoles) {
      if (this.getRoles().indexOf(role.toLowerCase()) > -1) {
        return true;
      }
    }
    return false;
  }

  /**
   * Participant Actions
   */

  canReadAllParticipants(): boolean {
    return this.hasRole(['viewer', 'editor', 'admin']);
  }

  canReadAParticipant(): boolean {
    return this.hasRole(['viewer', 'editor', 'admin']);
  }

  canAddParticipant(): boolean {
    return this.hasRole(['editor', 'admin']);
  }

  canDeleteParticipant(): boolean {
    return this.hasRole(['editor', 'admin']);
  }

  canEditParticipant(): boolean {
    return this.hasRole(['editor', 'admin']);
  }

  /**
   * Club Actions
   */

  canReadAllClubs(): boolean {
    return this.hasRole(['viewer', 'editor', 'admin']);
  }

  canReadAClub(): boolean {
    return this.hasRole(['viewer', 'editor', 'admin']);
  }

  canAddClub(): boolean {
    return this.hasRole(['editor', 'admin']);
  }

  canDeleteClub(): boolean {
    return this.hasRole(['editor', 'admin']);
  }

  canEditClub(): boolean {
    return this.hasRole(['editor', 'admin']);
  }

  /**
   * Tournament Actions
   */

  canReadAllTournaments(): boolean {
    return this.hasRole(['viewer', 'editor', 'admin']);
  }

  canReadATournament(): boolean {
    return this.hasRole(['viewer', 'editor', 'admin']);
  }

  canAddTournament(): boolean {
    return this.hasRole(['editor', 'admin']);
  }

  canDeleteTournament(): boolean {
    return this.hasRole(['editor', 'admin']);
  }

  canEditTournament(): boolean {
    return this.hasRole(['editor', 'admin']);
  }

  /**
   * Role Actions
   */

  canReadAllRoles(): boolean {
    return this.hasRole(['viewer', 'editor', 'admin']);
  }

  canReadARole(): boolean {
    return this.hasRole(['viewer', 'editor', 'admin']);
  }

  canAddRole(): boolean {
    return this.hasRole(['editor', 'admin']);
  }

  canDeleteRole(): boolean {
    return this.hasRole(['editor', 'admin']);
  }

  canEditRole(): boolean {
    return this.hasRole(['editor', 'admin']);
  }

  /**
   * Team Actions
   */

  canReadAllTeams(): boolean {
    return this.hasRole(['viewer', 'editor', 'admin']);
  }

  canReadATeam(): boolean {
    return this.hasRole(['viewer', 'editor', 'admin']);
  }

  canAddTeam(): boolean {
    return this.hasRole(['editor', 'admin']);
  }

  canDeleteTeam(): boolean {
    return this.hasRole(['editor', 'admin']);
  }

  canEditTeam(): boolean {
    return this.hasRole(['editor', 'admin']);
  }

  /**
   * Fight Actions
   */

  canReadAllFights(): boolean {
    return this.hasRole(['viewer', 'editor', 'admin']);
  }

  canReadAFight(): boolean {
    return this.hasRole(['viewer', 'editor', 'admin']);
  }

  canAddFight(): boolean {
    return this.hasRole(['editor', 'admin']);
  }

  canDeleteFight(): boolean {
    return this.hasRole(['editor', 'admin']);
  }

  canEditFight(): boolean {
    return this.hasRole(['editor', 'admin']);
  }

  /**
   * Duel Actions
   */

  canReadAllDuels(): boolean {
    return this.hasRole(['viewer', 'editor', 'admin']);
  }

  canReadADuel(): boolean {
    return this.hasRole(['viewer', 'editor', 'admin']);
  }

  canAddDuel(): boolean {
    return this.hasRole(['editor', 'admin']);
  }

  canDeleteDuel(): boolean {
    return this.hasRole(['editor', 'admin']);
  }

  canEditDuel(): boolean {
    return this.hasRole(['editor', 'admin']);
  }

  /**
   * Group Actions
   */

  canReadAllGroups(): boolean {
    return this.hasRole(['viewer', 'editor', 'admin']);
  }

  canReadAGroup(): boolean {
    return this.hasRole(['viewer', 'editor', 'admin']);
  }

  canAddGroup(): boolean {
    return this.hasRole(['editor', 'admin']);
  }

  canDeleteGroup(): boolean {
    return this.hasRole(['editor', 'admin']);
  }

  canEditGroup(): boolean {
    return this.hasRole(['editor', 'admin']);
  }

  /**
   * Ranking Actions
   */

  canReadAllRankings(): boolean {
    return this.hasRole(['viewer', 'editor', 'admin']);
  }

  canReadARanking(): boolean {
    return this.hasRole(['viewer', 'editor', 'admin']);
  }

  /**
   * User Actions
   */

  canReadAllUsers(): boolean {
    return this.hasRole(['admin']);
  }

  canReadAUser(): boolean {
    return this.hasRole(['admin']);
  }

  canUpdateAUser(): boolean {
    return this.hasRole(['admin']);
  }

  canDeleteAUser(): boolean {
    return this.hasRole(['admin']);
  }

}
