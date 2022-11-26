import {Injectable} from '@angular/core';
import {BehaviorSubject} from "rxjs";
import {Duel} from "../../models/duel";

@Injectable({
  providedIn: 'root'
})
export class FilterResetService {

  public resetFilter: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

  constructor() {
  }
}
