import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from "@angular/common/http";
import {EnvironmentService} from "../environment.service";
import {LoggerService} from "../logger.service";
import {AuthenticatedUserService} from "./authenticated-user.service";
import {Observable, of} from "rxjs";

import {catchError, tap} from "rxjs/operators";
import {Role} from "../models/role";
import {RoleType} from "../models/RoleType";

@Injectable({
  providedIn: 'root'
})
export class RoleService {

  private baseUrl = this.environmentService.getBackendUrl() + '/roles';

  httpOptions = {
    headers: new HttpHeaders({
      'Content-Type': 'application/json',
      'Authorization': 'Bearer ' + this.authenticatedUserService.getJwtValue()
    })
  };

  constructor(private http: HttpClient, private environmentService: EnvironmentService, private loggerService: LoggerService,
              public authenticatedUserService: AuthenticatedUserService) {
  }

  getAll(): Observable<Role[]> {
    const url: string = `${this.baseUrl}/`;
    return this.http.get<Role[]>(url, this.httpOptions)
      .pipe(
        tap(_ => this.log(`fetched all roles`)),
        catchError(this.handleError<Role[]>(`gets all`))
      );
  }

  get(id: number): Observable<Role> {
    const url: string = `${this.baseUrl}/${id}`;
    return this.http.get<Role>(url, this.httpOptions)
      .pipe(
        tap(_ => this.log(`fetched role id=${id}`)),
        catchError(this.handleError<Role>(`get id=${id}`))
      );
  }

  getFromTournament(id: number): Observable<Role> {
    const url: string = `${this.baseUrl}/tournaments/${id}`;
    return this.http.get<Role>(url, this.httpOptions)
      .pipe(
        tap(_ => this.log(`fetched roles from tournament id=${id}`)),
        catchError(this.handleError<Role>(`get from tournament id=${id}`))
      );
  }

  getFromTournamentAndType(id: number, type: RoleType): Observable<Role[]> {
    const url: string = `${this.baseUrl}/tournaments/${id}/types/` + type;
    return this.http.get<Role[]>(url, this.httpOptions)
      .pipe(
        tap(_ => this.log(`fetched roles from tournament id=${id}`)),
        catchError(this.handleError<Role[]>(`get from tournament id=${id}`))
      );
  }

  getFromTournamentAndTypes(id: number, types: RoleType[]): Observable<Role[]> {
    const url: string = `${this.baseUrl}/tournaments/${id}/types/` + types.join(',');
    return this.http.get<Role[]>(url, this.httpOptions)
      .pipe(
        tap(_ => this.log(`fetched roles from tournament id=${id}`)),
        catchError(this.handleError<Role[]>(`get from tournament id=${id}`))
      );
  }

  deleteById(id: number): Observable<number> {
    const url: string = `${this.baseUrl}/${id}`;
    return this.http.delete<number>(url, this.httpOptions)
      .pipe(
        tap(_ => this.log(`deleting role id=${id}`)),
        catchError(this.handleError<number>(`delete id=${id}`))
      );
  }

  delete(Role: Role): Observable<Role> {
    const url: string = `${this.baseUrl}/delete`;
    return this.http.post<Role>(url, Role, this.httpOptions)
      .pipe(
        tap(_ => this.log(`deleting role ${Role}`)),
        catchError(this.handleError<Role>(`delete ${Role}`))
      );
  }

  add(Role: Role): Observable<Role> {
    const url: string = `${this.baseUrl}/`;
    return this.http.post<Role>(url, Role, this.httpOptions)
      .pipe(
        tap((newRole: Role) => this.log(`adding role ${newRole}`)),
        catchError(this.handleError<Role>(`adding ${Role}`))
      );
  }

  update(Role: Role): Observable<Role> {
    const url: string = `${this.baseUrl}/`;
    return this.http.put<Role>(url, Role, this.httpOptions)
      .pipe(
        tap((updatedRole: Role) => this.log(`updating role ${updatedRole}`)),
        catchError(this.handleError<Role>(`updating ${Role}`))
      );
  }

  private log(message: string) {
    this.loggerService.add(`RoleService: ${message}`);
  }

  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {

      // TODO: send the error to remote logging infrastructure
      console.error(error); // log to console instead

      // TODO: better job of transforming error for participant consumption
      this.log(`${operation} failed: ${error.message}`);

      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }
}
