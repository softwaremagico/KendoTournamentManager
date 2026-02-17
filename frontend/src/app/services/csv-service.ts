import {Injectable} from "@angular/core";
import {HttpClient} from "@angular/common/http";
import {EnvironmentService} from "../environment.service";
import {MessageService} from "./message.service";
import {LoggerService} from "./logger.service";
import {LoginService} from "./login.service";
import {SystemOverloadService} from "./notifications/system-overload.service";
import {Club} from "../models/club";
import {Observable} from "rxjs";
import {tap} from "rxjs/operators";
import {Participant} from "../models/participant";
import {Team} from "../models/team";
import {GroupLink} from "../models/group-link.model";

@Injectable({
  providedIn: 'root'
})
export class CsvService {

  private baseUrl: string = this.environmentService.getBackendUrl() + '/csv';

  constructor(private http: HttpClient, private environmentService: EnvironmentService, private messageService: MessageService,
              private loggerService: LoggerService, public loginService: LoginService,
              private systemOverloadService: SystemOverloadService) {
  }


  addClubs(file: File): Observable<Club[]> {
    this.systemOverloadService.isBusy.next(true);
    let url: string = `${this.baseUrl}/clubs`;
    const formData = new FormData();
    formData.append("file", file);
    formData.append("reportProgress", "true");
    return this.http.post<Club[]>(url, formData)
      .pipe(
        tap({
          next: (_clubs: Club[]) => this.loggerService.info(`adding clubs as csv`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        })
      );
  }


  addParticipants(file: File): Observable<Participant[]> {
    this.systemOverloadService.isBusy.next(true);
    let url: string = `${this.baseUrl}/participants`;
    const formData = new FormData();
    formData.append("file", file);
    formData.append("reportProgress", "true");
    return this.http.post<Participant[]>(url, formData)
      .pipe(
        tap({
          next: (_participants: Participant[]) => this.loggerService.info(`adding participants as csv`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        })
      );
  }


  addTeams(file: File): Observable<Team[]> {
    this.systemOverloadService.isBusy.next(true);
    let url: string = `${this.baseUrl}/teams`;
    const formData = new FormData();
    formData.append("file", file);
    formData.append("reportProgress", "true");
    return this.http.post<Team[]>(url, formData)
      .pipe(
        tap({
          next: (_teams: Team[]) => this.loggerService.info(`adding teams as csv`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        })
      );
  }


  addGroupLinks(file: File, tournamentId: number): Observable<GroupLink[]> {
    this.systemOverloadService.isBusy.next(true);
    let url: string = `${this.baseUrl}/groups-link/tournaments/${tournamentId}`;
    const formData = new FormData();
    formData.append("file", file);
    formData.append("reportProgress", "true");
    return this.http.post<GroupLink[]>(url, formData)
      .pipe(
        tap({
          next: (_groupLinks: GroupLink[]) => this.loggerService.info(`adding group links as csv`),
          error: () => this.systemOverloadService.isBusy.next(false),
          complete: () => this.systemOverloadService.isBusy.next(false),
        })
      );
  }

}
