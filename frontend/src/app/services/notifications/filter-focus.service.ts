import {Injectable} from '@angular/core';
import {BehaviorSubject} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class FilterFocusService {

  public isFilterActive: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

}
