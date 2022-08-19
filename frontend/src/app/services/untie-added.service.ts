import {Injectable} from '@angular/core';
import {BehaviorSubject} from "rxjs";
import {Duel} from "../models/duel";

@Injectable({
  providedIn: 'root'
})
export class UntieAddedService {

  public isDuelAdded: BehaviorSubject<Duel> = new BehaviorSubject<Duel>(new Duel());

  constructor() {
  }
}
