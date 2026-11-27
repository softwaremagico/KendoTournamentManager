import {ServiceRequestMonitor} from './service-request-monitor';
import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders, HttpResponse} from "@angular/common/http";
import {EnvironmentService} from "../environment.service";
import {MessageService} from "./message.service";
import {LoggerService} from "./logger.service";
import {LoginService} from "./login.service";
import {Observable} from "rxjs";
import {catchError, map, tap} from "rxjs/operators";
import {Fight} from "../models/fight";
import {Tournament} from "../models/tournament";
import {SystemOverloadService} from "./notifications/system-overload.service";

/**
 * Angular service for all fight-related HTTP operations.
 *
 * Wraps the backend {@code /fights} REST endpoints. Provides methods to:
 * - Retrieve all fights or fights filtered by tournament.
 * - Create, update, and delete individual fights or collections.
 * - Generate the next round of fights for dynamic tournament formats.
 * - Download fight lists as PDF documents.
 *
 * All HTTP calls update the system-overload indicator on completion or error.
 */
@Injectable({
  providedIn: 'root'
})
export class FightService {
  private readonly monitor = new ServiceRequestMonitor(this.loggerService, this.messageService, this.systemOverloadService);


  private readonly baseUrl: string = this.environmentService.getBackendUrl() + '/fights';

  constructor(private readonly http: HttpClient, private readonly environmentService: EnvironmentService, private readonly messageService: MessageService,
              private readonly loggerService: LoggerService, public loginService: LoginService,
              private readonly systemOverloadService: SystemOverloadService) {

  }

  getAll(): Observable<Fight[]> {
    const url: string = `${this.baseUrl}`;
    return this.http.get<Fight[]>(url)
      .pipe(this.monitor.track<Fight[]>(`gets all`, () => `fetched all fights`));
  }

  getFromTournament(tournament: Tournament): Observable<Fight[]> {
    const url: string = `${this.baseUrl}/tournaments/${tournament.id}`;
    return this.http.get<Fight[]>(url)
      .pipe(this.monitor.track<Fight[]>(`get from tournament ${tournament.name}`, () => `fetched fights from tournament ${tournament.name}`));
  }

  deleteById(id: number): Observable<number> {
    const url: string = `${this.baseUrl}/${id}`;
    return this.http.delete<number>(url)
      .pipe(this.monitor.track<number>(`delete id=${id}`, () => `deleting fight id=${id}`));
  }

  delete(fight: Fight): Observable<Fight> {
    const url: string = `${this.baseUrl}/delete`;
    return this.http.post<Fight>(url, fight)
      .pipe(this.monitor.track<Fight>(`delete ${fight.id}`, () => `deleting fight ${fight.id}`));
  }

  deleteCollection(fights: Fight[]): Observable<Fight[]> {
    const url: string = `${this.baseUrl}/delete/list`;
    return this.http.post<Fight[]>(url, fights)
      .pipe(this.monitor.track<Fight[]>(`delete ${fights.length}`, () => `deleting fights ${fights.length}`));
  }

  deleteByTournament(tournament: Tournament): Observable<Fight> {
    const url: string = `${this.baseUrl}/delete/tournaments`;
    return this.http.post<Fight>(url, {tournament: tournament})
      .pipe(this.monitor.track<Fight>(`delete fights on ${tournament.name}`, () => `deleting fights on ${tournament.name}`));
  }

  add(fight: Fight): Observable<Fight> {
    const url: string = `${this.baseUrl}`;
    return this.http.post<Fight>(url, fight)
      .pipe(this.monitor.track<Fight>(`adding fight`));
  }

  addCollection(fights: Fight[]): Observable<Fight[]> {
    const url: string = `${this.baseUrl}` + '/list';
    return this.http.post<Fight[]>(url, fights)
      .pipe(this.monitor.track<Fight[]>(`adding fight`));
  }

  update(fight: Fight): Observable<Fight> {
    const url: string = `${this.baseUrl}`;
    return this.http.put<Fight>(url, fight)
      .pipe(this.monitor.track<Fight>(`updating fight`));
  }

  updateAll(fights: Fight[]): Observable<Fight[]> {
    const url: string = `${this.baseUrl}/all`;
    return this.http.put<Fight[]>(url, fights)
      .pipe(this.monitor.track<Fight[]>(`updating fight`));
  }

  create(tournamentId: number, level: number): Observable<Fight[]> {
    const url: string = `${this.baseUrl}` + '/create/tournaments/' + tournamentId + '/levels/' + level;
    return this.http.put<Fight[]>(url, undefined)
      .pipe(this.monitor.track<Fight[]>(`adding fight`));
  }

  createNext(tournamentId: number): Observable<Fight[]> {
    const url: string = `${this.baseUrl}` + '/create/tournaments/' + tournamentId + '/next';
    return this.http.put<Fight[]>(url, undefined, {observe: 'response'})
      .pipe(
        map((response: HttpResponse<Fight[]>) => {
          //204 means that fights are not created due to an existing draw score.
          if (response.status === 204) {
            return [];
          }
          return response.body ?? [];
        })
      );
  }

  getFightSummaryPDf(tournamentId: number): Observable<Blob> {
    const url: string = `${this.baseUrl}` + '/tournaments/' + tournamentId + '/pdf';
    return this.http.get<Blob>(url, {
      responseType: 'blob' as 'json', observe: 'body', headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    }).pipe(this.monitor.track<Blob>(`getting fight summary`, () => `getting fights summary`));
  }

  generateDuels(fight: Fight): Observable<Fight> {
    const url: string = `${this.baseUrl}/duels`;
    return this.http.put<Fight>(url, fight)
      .pipe(this.monitor.track<Fight>(`generating duels for a fight`));
  }


  getFromParticipant(competitorId: number): Observable<Fight[]> {
    const url: string = `${this.baseUrl}/competitor/` + competitorId;
    return this.http.get<Fight[]>(url)
      .pipe(
        tap((_fights: Fight[]) => this.loggerService.info(`getting fights from participant '` + competitorId + `'`)),
        catchError(this.messageService.handleError<Fight[]>(`getting fights from participant '` + competitorId + `'`))
      );
  }
}
