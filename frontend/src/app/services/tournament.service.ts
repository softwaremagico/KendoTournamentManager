import {Injectable} from '@angular/core';
import {HttpClient, HttpHeaders, HttpParams} from "@angular/common/http";
import {EnvironmentService} from "../environment.service";
import {LoginService} from "./login.service";
import {Observable} from "rxjs";
import {Tournament} from "../models/tournament";
import {catchError, tap} from "rxjs/operators";
import {MessageService} from "./message.service";
import {LoggerService} from "./logger.service";
import {SystemOverloadService} from "./notifications/system-overload.service";
import {Participant} from "../models/participant";
import {RoleType} from "../models/role-type";

@Injectable({
    providedIn: 'root'
})
export class TournamentService {

    private baseUrl: string = this.environmentService.getBackendUrl() + '/tournaments';

    constructor(private http: HttpClient, private environmentService: EnvironmentService,
                public loginService: LoginService, private messageService: MessageService,
                private systemOverloadService: SystemOverloadService,
                private loggerService: LoggerService) {
    }

    getAll(): Observable<Tournament[]> {
        const url: string = `${this.baseUrl}`;

        return this.http.get<Tournament[]>(url)
            .pipe(
                tap({
                    next: () => this.loggerService.info(`fetched all Tournaments`),
                    error: () => this.systemOverloadService.isBusy.next(false),
                    complete: () => this.systemOverloadService.isBusy.next(false),
                }),
                catchError(this.messageService.handleError<Tournament[]>(`gets all`))
            );
    }

    get(id: number): Observable<Tournament> {
        const url: string = `${this.baseUrl}/${id}`;
        return this.http.get<Tournament>(url)
            .pipe(
                tap({
                    next: () => this.loggerService.info(`fetched tournament id=${id}`),
                    error: () => this.systemOverloadService.isBusy.next(false),
                    complete: () => this.systemOverloadService.isBusy.next(false),
                }),
                catchError(this.messageService.handleError<Tournament>(`get id=${id}`))
            );
    }

    deleteById(id: number) {
        const url: string = `${this.baseUrl}/${id}`;
        this.http.delete(url)
            .pipe(
                tap({
                    next: () => this.loggerService.info(`deleting tournament id=${id}`),
                    error: () => this.systemOverloadService.isBusy.next(false),
                    complete: () => this.systemOverloadService.isBusy.next(false),
                }),
                catchError(this.messageService.handleError<Tournament>(`delete id=${id}`))
            );
    }

    delete(tournament: Tournament): Observable<Tournament> {
        const url: string = `${this.baseUrl}/delete`;
        return this.http.post<Tournament>(url, tournament)
            .pipe(
                tap({
                    next: () => this.loggerService.info(`deleting tournament ${tournament}`),
                    error: () => this.systemOverloadService.isBusy.next(false),
                    complete: () => this.systemOverloadService.isBusy.next(false),
                }),
                catchError(this.messageService.handleError<Tournament>(`delete ${tournament}`))
            );
    }

    add(tournament: Tournament): Observable<Tournament> {
        const url: string = `${this.baseUrl}`;
        return this.http.post<Tournament>(url, tournament)
            .pipe(
                tap({
                    next: () => (newTournament: Tournament) => this.loggerService.info(`adding tournament ${newTournament}`),
                    error: () => this.systemOverloadService.isBusy.next(false),
                    complete: () => this.systemOverloadService.isBusy.next(false),
                }),
                catchError(this.messageService.handleError<Tournament>(`adding ${tournament}`))
            );
    }


    update(tournament: Tournament): Observable<Tournament> {
        const url: string = `${this.baseUrl}`;
        this.systemOverloadService.isBusy.next(true);
        return this.http.put<Tournament>(url, tournament)
            .pipe(
                tap({
                    next: () => (updatedTournament: Tournament) => this.loggerService.info(`updating tournament ${updatedTournament}`),
                    error: () => this.systemOverloadService.isBusy.next(false),
                    complete: () => this.systemOverloadService.isBusy.next(false),
                }),
                catchError(this.messageService.handleError<Tournament>(`updating ${tournament}`))
            );
    }

    clone(id: number): Observable<Tournament> {
        const url: string = `${this.baseUrl}/${id}/clone`;
        return this.http.get<Tournament>(url)
            .pipe(
                tap({
                    next: () => this.loggerService.info(`cloned tournament id=${id}`),
                    error: () => this.systemOverloadService.isBusy.next(false),
                    complete: () => this.systemOverloadService.isBusy.next(false),
                }),
                catchError(this.messageService.handleError<Tournament>(`clone id=${id}`))
            );
    }

    setNumberOfWinners(tournament: Tournament, numberOfWinners: number): Observable<Tournament> {
        const url: string = `${this.baseUrl}/${tournament.id}/winners/${numberOfWinners}`;
        this.systemOverloadService.isBusy.next(true);
        return this.http.put<Tournament>(url, null)
            .pipe(
                tap({
                    next: () => (updatedTournament: Tournament) => this.loggerService.info(`updating tournament ${updatedTournament}`),
                    error: () => this.systemOverloadService.isBusy.next(false),
                    complete: () => this.systemOverloadService.isBusy.next(false),
                }),
                catchError(this.messageService.handleError<Tournament>(`updating ${tournament}`))
            );
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
        }).pipe(
            tap({
                next: () => this.loggerService.info(`getting tournament accreditations`),
                error: () => this.systemOverloadService.isBusy.next(false),
                complete: () => this.systemOverloadService.isBusy.next(false),
            }),
            catchError(this.messageService.handleError<Blob>(`getting tournament accreditations`))
        );
    }

    getParticipantAccreditation(tournamentId: number, participant: Participant, roleType: RoleType | undefined): Observable<Blob> {
        this.systemOverloadService.isBusy.next(true);
        if (roleType === undefined) {
            roleType = RoleType.getRandom();
        }
        const url: string = `${this.baseUrl}/` + tournamentId + '/accreditations/' + roleType;
        return this.http.post<Blob>(url, participant, {
            responseType: 'blob' as 'json', observe: 'body', headers: new HttpHeaders({
                'Content-Type': 'application/json'
            })
        }).pipe(
            tap({
                next: () => this.loggerService.info(`getting participant accreditations`),
                error: () => this.systemOverloadService.isBusy.next(false),
                complete: () => this.systemOverloadService.isBusy.next(false),
            }),
            catchError(this.messageService.handleError<Blob>(`getting participant accreditations`))
        );
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
        }).pipe(
            tap({
                next: () => this.loggerService.info(`getting tournament diplomas`),
                error: () => this.systemOverloadService.isBusy.next(false),
                complete: () => this.systemOverloadService.isBusy.next(false),
            }),
            catchError(this.messageService.handleError<Blob>(`getting tournament diplomas`))
        );
    }

    getParticipantDiploma(tournamentId: number, participant: Participant): Observable<Blob> {
        this.systemOverloadService.isBusy.next(true);
        const url: string = `${this.baseUrl}/` + tournamentId + '/diplomas';
        return this.http.post<Blob>(url, participant, {
            responseType: 'blob' as 'json', observe: 'body', headers: new HttpHeaders({
                'Content-Type': 'application/json'
            })
        }).pipe(
            tap({
                next: () => this.loggerService.info(`getting participant diplomas`),
                error: () => this.systemOverloadService.isBusy.next(false),
                complete: () => this.systemOverloadService.isBusy.next(false),
            }),
            catchError(this.messageService.handleError<Blob>(`getting participant diplomas`))
        );
    }

}
