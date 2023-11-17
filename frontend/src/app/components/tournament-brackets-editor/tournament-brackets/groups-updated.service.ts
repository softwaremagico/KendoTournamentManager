import {Injectable} from '@angular/core';
import {BehaviorSubject} from "rxjs";
import {Group} from "../../../models/group";
import {Team} from "../../../models/team";

@Injectable({
  providedIn: 'root'
})
export class GroupsUpdatedService {

  public areGroupsUpdated: BehaviorSubject<Group[]> = new BehaviorSubject<Group[]>([]);

  public areTeamListUpdated: BehaviorSubject<Team[]> = new BehaviorSubject<Team[]>([]);

  public areRelationsUpdated: BehaviorSubject<Map<number, {
    src: number,
    dest: number,
    winner: number
  }[]>> = new BehaviorSubject<Map<number, { src: number, dest: number, winner: number }[]>>(new Map());

  public areTotalTeamsNumberUpdated: BehaviorSubject<number> = new BehaviorSubject<number>(0);
}
