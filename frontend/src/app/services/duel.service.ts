import {Injectable} from '@angular/core';
import {HttpClient} from "@angular/common/http";
import {EnvironmentService} from "../environment.service";
import {MessageService} from "./message.service";
import {LoggerService} from "./logger.service";
import {AuthenticatedUserService} from "./authenticated-user.service";
import {Observable} from "rxjs";
import {catchError, tap} from "rxjs/operators";
import {Duel} from "../models/duel";

@Injectable({
  providedIn: 'root'
})
export class DuelService {

  private baseUrl = this.environmentService.getBackendUrl() + '/fights/duels';

  constructor(private http: HttpClient, private environmentService: EnvironmentService, private messageService: MessageService,
              private loggerService: LoggerService, public authenticatedUserService: AuthenticatedUserService) {

  }

  update(duel: Duel): Observable<Duel> {
    const url: string = `${this.baseUrl}`;
    return this.http.put<Duel>(url, duel, this.authenticatedUserService.httpOptions)
      .pipe(
        tap((_updatedDuel: Duel) => this.loggerService.info(`updating duel`)),
        catchError(this.messageService.handleError<Duel>(`updating duel`))
      );
  }
}
