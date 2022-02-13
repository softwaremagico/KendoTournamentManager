import {Participant} from "../../../models/participant";

export class UserListData {
  participants: Participant[];
  filteredParticipants: Participant[];

  constructor() {
  }

  initParticipants(participants: Participant[]) {
    this.filteredParticipants = Object.assign([], participants);
  }

  filter(filter: string) {
    this.filteredParticipants = this.participants.filter(user => user.lastname.toLowerCase().includes(filter) ||
      user.name.toLowerCase().includes(filter) || user.idCard.toLowerCase().includes(filter));
  }

  getRealIndex(currentIndex: number): number {
    //If filter is used, the index of the user is incorrect. Convert it
    return this.participants.indexOf(this.filteredParticipants[currentIndex]);
  }


}
