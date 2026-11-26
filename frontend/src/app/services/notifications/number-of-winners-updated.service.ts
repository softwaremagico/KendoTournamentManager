import {Injectable} from '@angular/core';
import {BehaviorSubject} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class NumberOfWinnersUpdatedService {

  public numberOfWinners: BehaviorSubject<number> = new BehaviorSubject<number>(1);
}
