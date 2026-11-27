import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {EnvironmentService} from "../environment.service";
import {MessageService} from "./message.service";
import {LoggerService} from "./logger.service";
import {LoginService} from "./login.service";
import {SystemOverloadService} from "./notifications/system-overload.service";
import {Observable} from "rxjs";
import {catchError, tap} from "rxjs/operators";
import {GroupLink} from "../models/group-link.model";

@Injectable({
  providedIn: 'root'
})
export class GroupLinkService {

  private readonly baseUrl: string = this.environmentService.getBackendUrl() + '/groups-links';

  constructor(private readonly http: HttpClient, private readonly environmentService: EnvironmentService, private readonly messageService: MessageService,
              private readonly loggerService: LoggerService, public readonly loginService: LoginService,
              private readonly systemOverloadService: SystemOverloadService) {
  }


  getFromTournament(tournamentId: number): Observable<GroupLink[]> {
    const url: string = `${this.baseUrl}/tournament/${tournamentId}`;
    return this.http.get<GroupLink[]>(url)
      .pipe(
        tap({
          next: () => this.loggerService.info(`fetched all groups`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<GroupLink[]>(`gets all`))
      );
  }
}
