import {ServiceRequestMonitor} from './service-request-monitor';
import {Injectable} from '@angular/core';
import { HttpClient, HttpHeaders, HttpParams } from "@angular/common/http";
import {EnvironmentService} from "../environment.service";
import {LoginService} from "./login.service";
import {Observable} from "rxjs";
import {Tournament} from "../models/tournament";
import {MessageService} from "./message.service";
import {LoggerService} from "./logger.service";
import {SystemOverloadService} from "./notifications/system-overload.service";
import {Participant} from "../models/participant";
import {RoleType} from "../models/role-type";

/**
 * Angular service for all tournament-related HTTP operations.
 *
 * Wraps the backend {@code /tournaments} REST endpoints and returns typed
 * {@link Observable}s. All requests automatically include the JWT Bearer token
 * injected by the HTTP interceptor.
 *
 * System-overload signals ({@link SystemOverloadService#isBusy}) are reset on
 * both error and completion so that loading indicators are always dismissed.
 */
@Injectable({
  providedIn: 'root'
})
export class TournamentService {
  private readonly monitor = new ServiceRequestMonitor(this.loggerService, this.messageService, this.systemOverloadService);


  private readonly baseUrl: string = this.environmentService.getBackendUrl() + '/tournaments';

  constructor(private readonly http: HttpClient, private readonly environmentService: EnvironmentService,
              public readonly loginService: LoginService, private readonly messageService: MessageService,
              private readonly systemOverloadService: SystemOverloadService,
              private readonly loggerService: LoggerService) {
  }

  getAll(): Observable<Tournament[]> {
    const url: string = `${this.baseUrl}`;

    return this.http.get<Tournament[]>(url)
      .pipe(this.monitor.track<Tournament[]>(`gets all`, () => `fetched all Tournaments`));
  }

  get(id: number): Observable<Tournament> {
    const url: string = `${this.baseUrl}/${id}`;
    return this.http.get<Tournament>(url)
      .pipe(this.monitor.track<Tournament>(`get id=${id}`, () => `fetched tournament id=${id}`));
  }

  deleteById(id: number) {
    const url: string = `${this.baseUrl}/${id}`;
    this.http.delete<Tournament>(url)
      .pipe(this.monitor.track<Tournament>(`delete id=${id}`, () => `deleting tournament id=${id}`));
  }

  delete(tournament: Tournament): Observable<Tournament> {
    const url: string = `${this.baseUrl}/delete`;
    return this.http.post<Tournament>(url, tournament)
      .pipe(this.monitor.track<Tournament>(`delete ${tournament.name}`, () => `deleting tournament ${tournament.name}`));
  }

  add(tournament: Tournament): Observable<Tournament> {
    const url: string = `${this.baseUrl}`;
    return this.http.post<Tournament>(url, tournament)
      .pipe(this.monitor.track<Tournament>(`adding ${tournament.name}`));
  }


  update(tournament: Tournament): Observable<Tournament> {
    const url: string = `${this.baseUrl}`;
    this.systemOverloadService.isBusy.next(true);
    return this.http.put<Tournament>(url, tournament)
      .pipe(this.monitor.track<Tournament>(`updating ${tournament.name}`));
  }

  clone(id: number): Observable<Tournament> {
    const url: string = `${this.baseUrl}/${id}/clone`;
    return this.http.get<Tournament>(url)
      .pipe(this.monitor.track<Tournament>(`clone id=${id}`, () => `cloned tournament id=${id}`));
  }

  setNumberOfWinners(tournament: Tournament, numberOfWinners: number): Observable<Tournament> {
    const url: string = `${this.baseUrl}/${tournament.id}/winners/${numberOfWinners}`;
    this.systemOverloadService.isBusy.next(true);
    return this.http.put<Tournament>(url, null)
      .pipe(this.monitor.track<Tournament>(`updating ${tournament.name}`));
  }

  getAccreditations(tournamentId: number, newOnes: boolean | undefined, roles: RoleType[]): Observable<Blob> {
    this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}/` + tournamentId + '/accreditations';
    return this.http.get<Blob>(url, {
      responseType: 'blob' as 'json', observe: 'body', headers: new HttpHeaders({
        'Content-Type': 'application/json'
      }),
      params: new HttpParams({
        fromObject: {
          'roles': roles,
          'onlyNew': newOnes!
        }
      })
    }).pipe(this.monitor.track<Blob>(`getting tournament accreditations`, () => `getting tournament accreditations`));
  }

  getParticipantAccreditation(tournamentId: number, participant: Participant, roleType: RoleType | undefined): Observable<Blob> {
    this.systemOverloadService.isBusy.next(true);
    roleType ??= RoleType.getRandom();
    const url: string = `${this.baseUrl}/` + tournamentId + '/accreditations/' + roleType;
    return this.http.post<Blob>(url, participant, {
      responseType: 'blob' as 'json', observe: 'body', headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    }).pipe(this.monitor.track<Blob>(`getting participant accreditations`, () => `getting participant accreditations`));
  }

  getDiplomas(tournamentId: number, newOnes: boolean | undefined, roles: RoleType[]): Observable<Blob> {
    this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}/` + tournamentId + '/diplomas';
    return this.http.get<Blob>(url, {
      responseType: 'blob' as 'json', observe: 'body', headers: new HttpHeaders({
        'Content-Type': 'application/json'
      }),
      params: new HttpParams({
        fromObject: {
          'roles': roles,
          'onlyNew': newOnes!
        }
      })
    }).pipe(this.monitor.track<Blob>(`getting tournament diplomas`, () => `getting tournament diplomas`));
  }

  getParticipantDiploma(tournamentId: number, participant: Participant): Observable<Blob> {
    this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}/` + tournamentId + '/diplomas';
    return this.http.post<Blob>(url, participant, {
      responseType: 'blob' as 'json', observe: 'body', headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    }).pipe(this.monitor.track<Blob>(`getting participant diplomas`, () => `getting participant diplomas`));
  }

  getLastUnlockedTournament(): Observable<Tournament> {
    const url: string = `${this.baseUrl}/unlocked/lasts`;
    return this.http.get<Tournament>(url)
      .pipe(this.monitor.track<Tournament>(`Cannot retrieve last unlocked tournament`));
  }

}
