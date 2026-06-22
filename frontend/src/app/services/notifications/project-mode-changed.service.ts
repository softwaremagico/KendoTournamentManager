import {Injectable} from '@angular/core';
import {BehaviorSubject} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class ProjectModeChangedService {

  public isProjectMode: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);
}
