import { Injectable } from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {EnvironmentService} from "../environment.service";
import {AuthenticatedUserService} from "./authenticated-user.service";
import {Observable} from "rxjs";
import {Tournament} from "../models/tournament";
import {catchError, tap} from "rxjs/operators";
import {MessageService} from "./message.service";

@Injectable({
  providedIn: 'root'
})
export class TournamentService {

  private baseUrl = this.environmentService.getBackendUrl() + '/tournaments';

  httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + this.authenticatedUserService.getJwtValue()
    })
  };

  constructor(private http: HttpClient, private environmentService: EnvironmentService,
              public authenticatedUserService: AuthenticatedUserService,  private messageService: MessageService) { }

  getAll(): Observable<Tournament[]> {
    const url: string = `${this.baseUrl}/`;
    return this.http.get<Tournament[]>(url, this.httpOptions)
      .pipe(
        tap(_ => this.messageService.log(`fetched all Tournaments`)),
        catchError(this.messageService.handleError<Tournament[]>(`gets all`))
      );
  }

  get(id: number): Observable<Tournament> {
    const url: string = `${this.baseUrl}/${id}`;
    return this.http.get<Tournament>(url, this.httpOptions)
      .pipe(
        tap(_ => this.messageService.log(`fetched tournament id=${id}`)),
        catchError(this.messageService.handleError<Tournament>(`get id=${id}`))
      );
  }

  deleteById(id: number) {
    const url: string = `${this.baseUrl}/${id}`;
    this.http.delete(url, this.httpOptions)
      .pipe(
        tap(_ => this.messageService.log(`deleting tournament id=${id}`)),
        catchError(this.messageService.handleError<Tournament>(`delete id=${id}`))
      );
  }

  delete(tournament: Tournament): Observable<Tournament> {
    const url: string = `${this.baseUrl}/delete`;
    return this.http.post<Tournament>(url, tournament, this.httpOptions)
      .pipe(
        tap(_ => this.messageService.log(`deleting tournament ${tournament}`)),
        catchError(this.messageService.handleError<Tournament>(`delete ${tournament}`))
      );
  }

  add(tournament: Tournament): Observable<Tournament> {
    const url: string = `${this.baseUrl}/`;
    return this.http.post<Tournament>(url, tournament, this.httpOptions)
      .pipe(
        tap((newTournament: Tournament) => this.messageService.log(`adding tournament ${newTournament}`)),
        catchError(this.messageService.handleError<Tournament>(`adding ${tournament}`))
      );
  }


  update(tournament: Tournament): Observable<Tournament> {
    const url: string = `${this.baseUrl}/`;
    return this.http.put<Tournament>(url, tournament, this.httpOptions)
      .pipe(
        tap((updatedTournament: Tournament) => this.messageService.log(`updating tournament ${updatedTournament}`)),
        catchError(this.messageService.handleError<Tournament>(`updating ${tournament}`))
      );
  }
}
