import {Injectable} from '@angular/core';
import {BehaviorSubject} from "rxjs";
import {Group} from "../../models/group";

@Injectable({
  providedIn: 'root'
})
export class GroupUpdatedService {

  public isGroupUpdated: BehaviorSubject<Group> = new BehaviorSubject<Group>(new Group());

}
