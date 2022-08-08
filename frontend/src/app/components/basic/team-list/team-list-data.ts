import {Team} from "../../../models/team";

export class teamListData {
  teams: Team[];
  filteredTeams: Team[];

  filter(filter: string) {
    this.filteredTeams = this.teams.filter(team => team.name.toLowerCase().includes(filter) ||
      team.members.some(user => (user.lastname.toLowerCase().includes(filter) ||
        user.name.toLowerCase().includes(filter) ||
        (user.club ? user.club.name.toLowerCase().includes(filter) : ""))));
  }

}
