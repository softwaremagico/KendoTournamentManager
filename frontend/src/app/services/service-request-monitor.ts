import {MonoTypeOperatorFunction, Observable} from 'rxjs';
import {catchError, tap} from 'rxjs/operators';
import {LoggerService} from './logger.service';
import {MessageService} from './message.service';
import {SystemOverloadService} from './notifications/system-overload.service';

/** Mensaje de éxito; `null` conserva operaciones que no registran log. */
type SuccessLog<T> = string | ((response: T) => string) | null;

/**
 * Operador compartido por los servicios HTTP: registra la respuesta, libera el
 * indicador de ocupación tanto al completar como al fallar y delega el error en
 * `MessageService`. Centraliza el comportamiento repetido en los servicios CRUD
 * de clubes, equipos, roles, torneos, combates y clasificaciones.
 */
export class ServiceRequestMonitor {

  constructor(private readonly loggerService: LoggerService,
              private readonly messageService: MessageService,
              private readonly systemOverloadService: SystemOverloadService) {
  }

  track<T>(errorContext: string, successLog: SuccessLog<T> = errorContext): MonoTypeOperatorFunction<T> {
    return (source: Observable<T>): Observable<T> => source.pipe(
      tap({
        next: (response: T) => {
          if (successLog !== null) {
            this.loggerService.info(typeof successLog === 'function' ? successLog(response) : successLog);
          }
        },
        error: () => this.systemOverloadService.isBusy.next(false),
        complete: () => this.systemOverloadService.isBusy.next(false),
      }),
      catchError(this.messageService.handleError<T>(errorContext))
    );
  }
}
