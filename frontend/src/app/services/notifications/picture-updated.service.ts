import {Injectable} from '@angular/core';
import {BehaviorSubject} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class PictureUpdatedService {

  public isPictureUpdated: BehaviorSubject<string> = new BehaviorSubject<string>("");
}
