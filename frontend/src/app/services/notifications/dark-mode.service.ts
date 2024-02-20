import {Injectable} from '@angular/core';
import {BehaviorSubject} from "rxjs";
import {UserSessionService} from "../user-session.service";

@Injectable({
  providedIn: 'root'
})
export class DarkModeService {

  constructor(public userSessionService: UserSessionService) {
  }

  public darkModeSwitched: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(this.userSessionService.getNightMode());
}
