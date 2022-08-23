import { Injectable } from '@angular/core';
import {BehaviorSubject} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class SystemOverloadService {

  public isBusy: BehaviorSubject<boolean> = new BehaviorSubject<boolean>(false);

  constructor() { }
}
