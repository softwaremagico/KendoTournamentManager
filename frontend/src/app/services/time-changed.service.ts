import {Injectable} from '@angular/core';
import {BehaviorSubject} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class TimeChangedService {

  public isElapsedTimeChanged: BehaviorSubject<number> = new BehaviorSubject<number>(0);
  public isTotalTimeChanged: BehaviorSubject<number> = new BehaviorSubject<number>(0);

  constructor() { }
}
