import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders} from '@angular/common/http';
import {EnvironmentService} from "../environment.service";
import {catchError, map, tap} from 'rxjs/operators';
import {Observable, of} from "rxjs";
import {User} from "../models/user";
import {LoggerService} from "../logger.service";

@Injectable({
  providedIn: 'root'
})
export class UserService {

  private baseUrl = this.environmentService.getBackendUrl() + '/users';

  httpOptions = {
    headers: new HttpHeaders({'Content-Type': 'application/json'})
  };

  constructor(private http: HttpClient, private environmentService: EnvironmentService, private loggerService: LoggerService) {
  }

  getAll(): Observable<User[]> {
    const url: string = `${this.baseUrl}/`;
    return this.http.get<User[]>(url)
      .pipe(
        tap(_ => this.log(`fetched all users`)),
        catchError(this.handleError<User[]>(`gets all`))
      );
  }

  get(id: number): Observable<User> {
    const url: string = `${this.baseUrl}/${id}`;
    return this.http.get<User>(url)
      .pipe(
        tap(_ => this.log(`fetched user id=${id}`)),
        catchError(this.handleError<User>(`get id=${id}`))
      );
  }

  delete(id: number) {
    const url: string = `${this.baseUrl}/${id}`;
    this.http.delete(url)
      .pipe(
        tap(_ => this.log(`deleting user id=${id}`)),
        catchError(this.handleError<User>(`delete id=${id}`))
      );
  }

  add(user: User): Observable<User> {
    const url: string = `${this.baseUrl}`;
    return this.http.post<User>(url, user, this.httpOptions)
      .pipe(
        tap((newUser: User) => this.log(`adding user ${newUser}`)),
        catchError(this.handleError<User>(`adding ${user}`))
      );
  }


  update(user: User): Observable<User> {
    const url: string = `${this.baseUrl}`;
    return this.http.put<User>(url, user, this.httpOptions)
      .pipe(
        tap((updatedUser: User) => this.log(`updating user ${updatedUser}`)),
        catchError(this.handleError<User>(`updating ${user}`))
      );
  }

  private log(message: string) {
    this.loggerService.add(`UserService: ${message}`);
  }

  private handleError<T>(operation = 'operation', result?: T) {
    return (error: any): Observable<T> => {

      // TODO: send the error to remote logging infrastructure
      console.error(error); // log to console instead

      // TODO: better job of transforming error for user consumption
      this.log(`${operation} failed: ${error.message}`);

      // Let the app keep running by returning an empty result.
      return of(result as T);
    };
  }
}
