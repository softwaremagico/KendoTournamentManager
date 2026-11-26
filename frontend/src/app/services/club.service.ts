import {ServiceRequestMonitor} from './service-request-monitor';
import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {EnvironmentService} from "../environment.service";
import {Observable} from "rxjs";
import {Club} from "../models/club";
import {LoginService} from "./login.service";
import {MessageService} from "./message.service";
import {LoggerService} from "./logger.service";
import {SystemOverloadService} from "./notifications/system-overload.service";


@Injectable({
  providedIn: 'root'
})
export class ClubService {
  private readonly monitor = new ServiceRequestMonitor(this.loggerService, this.messageService, this.systemOverloadService);


  private baseUrl: string = this.environmentService.getBackendUrl() + '/clubs';

  constructor(private readonly http: HttpClient, private readonly environmentService: EnvironmentService, private readonly messageService: MessageService,
              private readonly loggerService: LoggerService, public readonly loginService: LoginService,
              private readonly systemOverloadService: SystemOverloadService) {
  }

  getAll(): Observable<Club[]> {
    const url: string = `${this.baseUrl}`;
    return this.http.get<Club[]>(url)
      .pipe(this.monitor.track<Club[]>(`gets all`, () => `fetched all clubs`));
  }

  get(id: number): Observable<Club> {
    const url: string = `${this.baseUrl}/${id}`;
    return this.http.get<Club>(url)
      .pipe(this.monitor.track<Club>(`get id=${id}`, () => `fetched club id=${id}`));
  }

  deleteById(id: number): Observable<number> {
    const url: string = `${this.baseUrl}/${id}`;
    return this.http.delete<number>(url)
      .pipe(this.monitor.track<number>(`delete id=${id}`, () => `deleting club id=${id}`));
  }

  delete(club: Club): Observable<Club> {
    const url: string = `${this.baseUrl}/delete`;
    return this.http.post<Club>(url, club)
      .pipe(this.monitor.track<Club>(`delete ${club.name}`, () => `deleting club ${club.name}`));
  }

  add(club: Club): Observable<Club> {
    const url: string = `${this.baseUrl}`;
    return this.http.post<Club>(url, club)
      .pipe(this.monitor.track<Club>(`adding ${club.name}`, (newClub: Club) => `adding club ${newClub.name}`));
  }

  update(club: Club): Observable<Club> {
    const url: string = `${this.baseUrl}`;
    return this.http.put<Club>(url, club)
      .pipe(this.monitor.track<Club>(`updating ${club.name}`, (updatedClub: Club) => `updating club ${updatedClub.name}`));
  }
}
