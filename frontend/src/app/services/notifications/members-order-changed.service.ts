import {Injectable} from '@angular/core';
import {BehaviorSubject} from "rxjs";
import {Fight} from "../../models/fight";

@Injectable({
  providedIn: 'root'
})
export class MembersOrderChangedService {

  public membersOrderChanged: BehaviorSubject<Fight> = new BehaviorSubject<Fight>(new Fight());

  public membersOrderAllowed: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

  constructor() {
  }
}
