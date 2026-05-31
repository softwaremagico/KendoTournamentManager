import {Injectable} from '@angular/core';
import {BehaviorSubject} from "rxjs";
import {Duel} from "../../models/duel";

@Injectable({
  providedIn: 'root'
})
export class ScoreUpdatedService {

  public isScoreUpdated: BehaviorSubject<Duel> = new BehaviorSubject<Duel>(new Duel());
}
