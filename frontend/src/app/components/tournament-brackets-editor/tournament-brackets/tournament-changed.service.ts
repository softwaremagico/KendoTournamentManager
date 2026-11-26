import {Injectable} from '@angular/core';
import {BehaviorSubject} from "rxjs";
import {Tournament} from "../../../models/tournament";

@Injectable({
  providedIn: 'root'
})
export class TournamentChangedService {

  public isTournamentChanged: BehaviorSubject<Tournament> = new BehaviorSubject<Tournament>(new Tournament());
}
