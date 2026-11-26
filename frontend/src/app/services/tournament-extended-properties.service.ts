import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {EnvironmentService} from "../environment.service";
import {LoginService} from "./login.service";
import {MessageService} from "./message.service";
import {SystemOverloadService} from "./notifications/system-overload.service";
import {LoggerService} from "./logger.service";
import {Tournament} from "../models/tournament";
import {Observable} from "rxjs";
import {catchError, tap} from "rxjs/operators";
import {TournamentExtendedProperty} from "../models/tournament-extended-property.model";
import {TournamentExtraPropertyKey} from "../models/tournament-extra-property-key";

@Injectable({
  providedIn: 'root'
})
export class TournamentExtendedPropertiesService {

  private readonly baseUrl: string = this.environmentService.getBackendUrl() + '/tournaments/properties';

  constructor(private readonly http: HttpClient, private readonly environmentService: EnvironmentService,
              public readonly loginService: LoginService, private readonly messageService: MessageService,
              private readonly systemOverloadService: SystemOverloadService,
              private readonly loggerService: LoggerService) {
  }

  getByTournament(tournament: Tournament): Observable<TournamentExtendedProperty[]> {
    const url: string = `${this.baseUrl}/tournaments/${tournament.id}`;
    return this.http.get<TournamentExtendedProperty[]>(url)
      .pipe(
        tap({
          next: () => this.loggerService.info(`fetched tournament properties from tournament ${tournament.name}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<TournamentExtendedProperty[]>(`getting properties from tournament ${tournament.name}`))
      );
  }

  getByTournamentAndKey(tournament: Tournament, propertyKey: TournamentExtraPropertyKey): Observable<TournamentExtendedProperty> {
    const url: string = `${this.baseUrl}/tournaments/${tournament.id}/key/${propertyKey}`;
    return this.http.get<TournamentExtendedProperty>(url)
      .pipe(
        tap({
          next: () => this.loggerService.info(`fetched tournament properties from tournament ${tournament.name} and key ${propertyKey}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<TournamentExtendedProperty>(`getting properties from tournament ${tournament.name} and key ${propertyKey}`))
      );
  }

  getLatest(): Observable<TournamentExtendedProperty[]> {
    const url: string = `${this.baseUrl}/latest`;
    return this.http.get<TournamentExtendedProperty[]>(url)
      .pipe(
        tap({
          next: () => this.loggerService.info('fetched latest tournament properties'),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<TournamentExtendedProperty[]>('getting latest tournament properties'))
      );
  }

  add(tournamentExtendedProperty: TournamentExtendedProperty): Observable<TournamentExtendedProperty> {
    const url: string = `${this.baseUrl}`;
    return this.http.post<TournamentExtendedProperty>(url, tournamentExtendedProperty)
      .pipe(
        tap({
          next: (newTournamentExtendedProperty: TournamentExtendedProperty) => this.loggerService.info(`adding property ${newTournamentExtendedProperty.propertyValue}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<TournamentExtendedProperty>(`adding ${tournamentExtendedProperty.propertyValue}`))
      );
  }

  update(tournamentExtendedProperty: TournamentExtendedProperty): Observable<TournamentExtendedProperty> {
    const url: string = `${this.baseUrl}`;
    return this.http.put<TournamentExtendedProperty>(url, tournamentExtendedProperty)
      .pipe(
        tap({
          next: (newTournamentExtendedProperty: TournamentExtendedProperty) => this.loggerService.info(`updating property ${newTournamentExtendedProperty.propertyValue}`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        }),
        catchError(this.messageService.handleError<TournamentExtendedProperty>(`updating ${tournamentExtendedProperty.propertyValue}`))
      );
  }
}
