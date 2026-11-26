import {ServiceRequestMonitor} from './service-request-monitor';
import {Injectable} from '@angular/core';
import { HttpClient, HttpHeaders } from "@angular/common/http";
import {EnvironmentService} from "../environment.service";
import {LoginService} from "./login.service";
import {Observable} from "rxjs";

import {Role} from "../models/role";
import {RoleType} from "../models/role-type";
import {Participant} from "../models/participant";
import {Tournament} from "../models/tournament";
import {MessageService} from "./message.service";
import {LoggerService} from "./logger.service";
import {SystemOverloadService} from "./notifications/system-overload.service";

@Injectable({
  providedIn: 'root'
})
export class RoleService {
  private readonly monitor = new ServiceRequestMonitor(this.loggerService, this.messageService, this.systemOverloadService);


  private baseUrl: string = this.environmentService.getBackendUrl() + '/roles';

  constructor(private readonly http: HttpClient, private readonly environmentService: EnvironmentService, private readonly messageService: MessageService,
              private readonly loggerService: LoggerService, private readonly systemOverloadService: SystemOverloadService, public readonly loginService: LoginService) {
  }

  getAll(): Observable<Role[]> {
    const url: string = `${this.baseUrl}`;
    return this.http.get<Role[]>(url)
      .pipe(this.monitor.track<Role[]>(`gets all`, () => `fetched all roles`));
  }

  get(id: number): Observable<Role> {
    const url: string = `${this.baseUrl}/${id}`;
    return this.http.get<Role>(url)
      .pipe(this.monitor.track<Role>(`get id=${id}`, () => `fetched role id=${id}`));
  }

  getFromTournament(id: number): Observable<Role[]> {
    const url: string = `${this.baseUrl}/tournaments/${id}`;
    return this.http.get<Role[]>(url)
      .pipe(this.monitor.track<Role[]>(`get from tournament id=${id}`, () => `fetched roles from tournament id=${id}`));
  }

  getFromTournamentAndType(id: number, type: RoleType): Observable<Role[]> {
    const url: string = `${this.baseUrl}/tournaments/${id}/types/` + type;
    return this.http.get<Role[]>(url)
      .pipe(this.monitor.track<Role[]>(`get from tournament id=${id}`, () => `fetched roles from tournament id=${id}`));
  }

  getFromTournamentAndTypes(id: number, types: RoleType[]): Observable<Role[]> {
    const url: string = `${this.baseUrl}/tournaments/${id}/types/` + types.join(',');
    return this.http.get<Role[]>(url)
      .pipe(this.monitor.track<Role[]>(`get from tournament id=${id}`, () => `fetched roles from tournament id=${id}`));
  }

  deleteById(id: number): Observable<number> {
    const url: string = `${this.baseUrl}/${id}`;
    return this.http.delete<number>(url)
      .pipe(this.monitor.track<number>(`delete id=${id}`, () => `deleting role id=${id}`));
  }

  delete(role: Role): Observable<Role> {
    const url: string = `${this.baseUrl}/delete`;
    return this.http.post<Role>(url, role)
      .pipe(this.monitor.track<Role>(`delete ${role.id}`, () => `deleting role ${role.id}`));
  }

  deleteByParticipantAndTournament(participant: Participant, tournament: Tournament): Observable<Role> {
    const url: string = `${this.baseUrl}/delete/participants`;
    return this.http.post<Role>(url, {
      participant: participant,
      tournament: tournament
    })
      .pipe(this.monitor.track<Role>(`delete role for ${participant} on ${tournament.name}`, () => `deleting role for ${participant} on ${tournament.name}`));
  }

  add(role: Role): Observable<Role> {
    const url: string = `${this.baseUrl}`;
    return this.http.post<Role>(url, role)
      .pipe(this.monitor.track<Role>(`adding ${role.id}`, (newRole: Role) => `adding role ${newRole.id}`));
  }

  update(role: Role): Observable<Role> {
    const url: string = `${this.baseUrl}`;
    return this.http.put<Role>(url, role)
      .pipe(this.monitor.track<Role>(`updating ${role.id}`, (updatedRole: Role) => `updating role ${updatedRole.id}`));
  }

  getRolesByTournament(tournamentId: number): Observable<Blob> {
    const url: string = `${this.baseUrl}` + '/tournaments/' + tournamentId + '/pdf';
    return this.http.get<Blob>(url, {
      responseType: 'blob' as 'json', observe: 'body', headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    });
  }
}
