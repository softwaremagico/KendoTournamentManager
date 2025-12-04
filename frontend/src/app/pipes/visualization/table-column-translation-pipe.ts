import {Pipe, PipeTransform} from '@angular/core';
import {TranslocoService} from "@ngneat/transloco";
import {TournamentType} from "../../models/tournament-type";
import {DatePipe} from "@angular/common";
import {UserSessionService} from "../../services/user-session.service";

@Pipe({
  name: 'tournamentType',
  standalone: true
})
export class TableColumnTranslationPipe implements PipeTransform {

  pipe: DatePipe;

  constructor(private transloco: TranslocoService, private userSessionService: UserSessionService) {
    this.setLocale();
  }

  private setLocale(): void {
    if (this.userSessionService.getLanguage() === 'es' || this.userSessionService.getLanguage() === 'ca') {
      this.pipe = new DatePipe('es');
    } else if (this.userSessionService.getLanguage() === 'it') {
      this.pipe = new DatePipe('it');
    } else if (this.userSessionService.getLanguage() === 'de') {
      this.pipe = new DatePipe('de');
    } else if (this.userSessionService.getLanguage() === 'nl') {
      this.pipe = new DatePipe('nl');
    } else {
      this.pipe = new DatePipe('en-US');
    }
  }

  transform(column: any): any {
    if (typeof column === 'number') {
      return column;
    } else if (typeof column === 'boolean') {
      return column ? this.transloco.translate('yes') : this.transloco.translate('no');
      //Is it a date?
    } else if (isNaN(column) && !isNaN(Date.parse(column)) && (column instanceof Date)) {
      return this.pipe.transform(column, 'short');
    } else if (column instanceof Object) {
      return this.transloco.translate(column.toString());
    } else {
      if (column) {
        const text: string = (column as string);
        if (text.toUpperCase() === text) {
          //probably is an enum
          return this.transloco.translate(this.snakeToCamel(text.toLowerCase()));
        } else {
          return this.transloco.translate(text);
        }
      } else {
        return "";
      }
    }
  }

  snakeToCamel(string: string): string {
    return string.toLowerCase().replace(/[-_][a-z]/g, (group: string) => group.slice(-1).toUpperCase());
  }

}
