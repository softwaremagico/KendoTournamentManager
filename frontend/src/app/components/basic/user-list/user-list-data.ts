import {Participant} from "../../../models/participant";

export class UserListData {
  participants: Participant[];
  filteredParticipants: Participant[];

  constructor() {
  }

  initParticipants(participants: Participant[]) {
    console.log("--> " + participants)
    this.filteredParticipants = Object.assign([], participants);
  }

  filter(filter: string) {
    this.filteredParticipants = this.participants.filter(user => user.lastname.toLowerCase().includes(filter) ||
      user.name.toLowerCase().includes(filter) || user.idCard.toLowerCase().includes(filter));
  }


}
