import {Injectable} from '@angular/core';
import {BehaviorSubject} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class DarkModeService {

  public darkModeSwitched: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
}
