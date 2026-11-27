import {Pipe, PipeTransform} from "@angular/core";
import {DatePipe} from "@angular/common";

@Pipe({
  name: 'customDatePipe',
  standalone: true
})
export class CustomDatePipe implements PipeTransform {

  constructor(private readonly _datePipe: DatePipe) {

  }

  transform(value: number | string | Date | null | undefined = 0): string | null {
    return this._datePipe.transform(value ?? 0, 'dd/MM/yyyy');
  }
}
