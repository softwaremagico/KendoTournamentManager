import {Pipe, PipeTransform} from "@angular/core";
import {DatePipe} from "@angular/common";

@Pipe({
  name: 'customDatePipe',
  standalone: true
})
export class CustomDatePipe implements PipeTransform {

  constructor(private readonly _datePipe: DatePipe) {

  }

  transform(value: number | string | Date | null | undefined): string | null {
    const normalizedValue: number | string | Date = value ?? 0;
    return this._datePipe.transform(normalizedValue, 'dd/MM/yyyy');
  }
}
