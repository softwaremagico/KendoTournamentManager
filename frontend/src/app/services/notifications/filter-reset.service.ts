import {Injectable} from '@angular/core';
import {BehaviorSubject} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class FilterResetService {

  public resetFilter: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
}
