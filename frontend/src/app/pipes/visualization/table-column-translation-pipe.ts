import {Pipe, PipeTransform} from '@angular/core';
import {TranslocoService} from '@jsverse/transloco';
import {DatePipe} from "@angular/common";
import {UserSessionService} from "../../services/user-session.service";
import {createLocaleDatePipe} from "../../utils/dates/locale-date-pipe";

@Pipe({
  name: 'tournamentType',
  standalone: true
})
export class TableColumnTranslationPipe implements PipeTransform {

  pipe: DatePipe;

  constructor(private readonly transloco: TranslocoService, private readonly userSessionService: UserSessionService) {
    this.setLocale();
  }

  private setLocale(): void {
    this.pipe = createLocaleDatePipe(this.userSessionService.getLanguage());
  }

  transform(column: unknown): unknown {
    if (typeof column === 'number') {
      return column;
    }
    if (typeof column === 'boolean') {
      return column ? this.transloco.translate('yes') : this.transloco.translate('no');
    }
    if (column instanceof Date) {
      return this.pipe.transform(column, 'short');
    }
    if (typeof column === 'string' && !Number.isNaN(Date.parse(column))) {
      return this.pipe.transform(new Date(column), 'short');
    }
    if (column === null || column === undefined || column === '') {
      return "";
    }

    const text: string = String(column);
    if (text.toUpperCase() === text) {
      // probably is an enum
      return this.transloco.translate(this.snakeToCamel(text.toLowerCase()));
    }
    return this.transloco.translate(text);
  }

  snakeToCamel(string: string): string {
    return string.toLowerCase().replace(/[-_][a-z]/g, (group: string) => group.slice(-1).toUpperCase());
  }

}
