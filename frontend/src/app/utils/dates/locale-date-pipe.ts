import {DatePipe} from "@angular/common";

/**
 * Construye el `DatePipe` con la configuración regional correspondiente al
 * idioma de la sesión. La asignación es la misma que usaban las vistas de
 * estadísticas y el pipe de columnas.
 */
export function createLocaleDatePipe(language: string): DatePipe {
  if (language === 'es' || language === 'ca') {
    return new DatePipe('es');
  } else if (language === 'it') {
    return new DatePipe('it');
  } else if (language === 'de') {
    return new DatePipe('de');
  } else if (language === 'nl') {
    return new DatePipe('nl');
  } else {
    return new DatePipe('en-US');
  }
}
