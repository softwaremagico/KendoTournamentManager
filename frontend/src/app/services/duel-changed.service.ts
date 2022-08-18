import {Injectable} from '@angular/core';
import {BehaviorSubject} from "rxjs";
import {Duel} from "../models/duel";

@Injectable({
  providedIn: 'root'
})
export class DuelChangedService {

  public isDuelSelected: BehaviorSubject<Duel> = new BehaviorSubject<Duel>(new Duel());

  constructor() { }
}
