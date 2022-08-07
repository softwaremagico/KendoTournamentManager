import {Participant} from "../../../models/participant";

export class UserListData {
  participants: Participant[];
  filteredParticipants: Participant[];

  filter(filter: string) {
    this.filteredParticipants = this.participants.filter(user => user.lastname.toLowerCase().includes(filter) ||
      user.name.toLowerCase().includes(filter) || user.idCard.toLowerCase().includes(filter) ||
      (user.club ? user.club.name.toLowerCase().includes(filter) : ""));
  }

}
