import {ServiceRequestMonitor} from './service-request-monitor';
import {Injectable} from '@angular/core';
import { HttpClient, HttpHeaders } from "@angular/common/http";
import {EnvironmentService} from "../environment.service";
import {LoginService} from "./login.service";
import {Observable} from "rxjs";
import {Team} from "../models/team";
import {Participant} from "../models/participant";
import {Tournament} from "../models/tournament";
import {MessageService} from "./message.service";
import {LoggerService} from "./logger.service";
import {SystemOverloadService} from "./notifications/system-overload.service";

@Injectable({
  providedIn: 'root'
})
export class TeamService {
  private readonly monitor = new ServiceRequestMonitor(this.loggerService, this.messageService, this.systemOverloadService);


  private readonly baseUrl: string = this.environmentService.getBackendUrl() + '/teams';

  constructor(private readonly http: HttpClient, private readonly environmentService: EnvironmentService, private readonly messageService: MessageService,
              private readonly loggerService: LoggerService, public readonly loginService: LoginService,
              private readonly systemOverloadService: SystemOverloadService) {
  }

  getAll(): Observable<Team[]> {
    const url: string = `${this.baseUrl}`;
    return this.http.get<Team[]>(url)
      .pipe(this.monitor.track<Team[]>(`gets all`, () => `fetched all teams`));
  }

  get(id: number): Observable<Team> {
    const url: string = `${this.baseUrl}/${id}`;
    return this.http.get<Team>(url)
      .pipe(this.monitor.track<Team>(`get id=${id}`, () => `fetched team id=${id}`));
  }

  getFromTournament(tournament: Tournament): Observable<Team[]> {
    const url: string = `${this.baseUrl}/tournaments/${tournament.id}`;
    return this.http.get<Team[]>(url)
      .pipe(this.monitor.track<Team[]>(`get from tournament ${tournament.name}`, () => `fetched teams from tournament ${tournament.name}`));
  }

  getRemainingFromTournament(tournament: Tournament): Observable<Team[]> {
    const url: string = `${this.baseUrl}/remaining/tournaments/${tournament.id}`;
    return this.http.get<Team[]>(url)
      .pipe(this.monitor.track<Team[]>(`get from tournament ${tournament.name}`, () => `fetched teams from tournament ${tournament.name}`));
  }


  deleteById(id: number): Observable<number> {
    const url: string = `${this.baseUrl}/${id}`;
    return this.http.delete<number>(url)
      .pipe(this.monitor.track<number>(`delete id=${id}`, () => `deleting team id=${id}`));
  }

  delete(team: Team): Observable<Team> {
    const url: string = `${this.baseUrl}/delete`;
    return this.http.post<Team>(url, team)
      .pipe(this.monitor.track<Team>(`delete ${team.name}`, () => `deleting team ${team.name}`));
  }

  deleteByMemberAndTournament(participant: Participant, tournament: Tournament): Observable<Team> {
    const url: string = `${this.baseUrl}/delete/members`;
    return this.http.post<Team>(url, {
      participant: participant,
      tournament: tournament
    })
      .pipe(this.monitor.track<Team>(`delete member ${participant} on ${tournament.name}`, () => `deleting member ${participant} on ${tournament.name}`));
  }

  deleteByMembersAndTournament(participants: Participant[], tournament: Tournament): Observable<Team> {
    const url: string = `${this.baseUrl}/delete/members/all`;
    return this.http.post<Team>(url, {
      participants: participants,
      tournament: tournament
    })
      .pipe(this.monitor.track<Team>(`delete members ${participants} on ${tournament.name}`, () => `deleting members ${participants} on ${tournament.name}`));
  }

  deleteByTournament(tournament: Tournament): Observable<Team> {
    const url: string = `${this.baseUrl}/tournaments/delete`;
    return this.http.post<Team>(url, {tournament: tournament})
      .pipe(this.monitor.track<Team>(`delete teams on ${tournament.name}`, () => `deleting teams on ${tournament.name}`));
  }

  createByTournament(tournament: Tournament): Observable<Team[]> {
    const url: string = `${this.baseUrl}/tournaments`;
    return this.http.put<Team[]>(url, {tournament: tournament})
      .pipe(this.monitor.track<Team[]>(`creating teams for ${tournament.name}`, () => `creating teams for ${tournament.name}`));
  }

  add(team: Team): Observable<Team> {
    const url: string = `${this.baseUrl}`;
    return this.http.post<Team>(url, team)
      .pipe(this.monitor.track<Team>(`adding ${team.name}`, (newTeam: Team) => `adding team ${newTeam.name}`));
  }

  setAll(teams: Team[]): Observable<Team[]> {
    this.systemOverloadService.isBusy.next(true);
    const url: string = `${this.baseUrl}/set`;
    return this.http.post<Team[]>(url, teams)
      .pipe(this.monitor.track<Team[]>(`adding ` + teams.length + "` teams.`"));
  }

  update(team: Team): Observable<Team> {
    const url: string = `${this.baseUrl}`;
    return this.http.put<Team>(url, team)
      .pipe(this.monitor.track<Team>(`updating ${team.name}`, (updatedTeam: Team) => `updating team ${updatedTeam.name}`));
  }

  getTeamsByTournament(tournamentId: number): Observable<Blob> {
    const url: string = `${this.baseUrl}` + '/tournaments/' + tournamentId + '/pdf';
    return this.http.get<Blob>(url, {
      responseType: 'blob' as 'json', observe: 'body', headers: new HttpHeaders({
        'Content-Type': 'application/json'
      })
    });
  }

}
