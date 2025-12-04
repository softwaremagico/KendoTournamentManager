import {Pipe, PipeTransform} from "@angular/core";
import {DatePipe} from "@angular/common";

@Pipe({
  name: 'customDatePipe',
  standalone: true
})
export class CustomDatePipe implements PipeTransform {

  constructor(private _datePipe: DatePipe) {

  }


  transform(value: any): any {
    value ? value = 0 : value;
    this._datePipe.transform(value, 'dd/MM/yyyy');
  }
}
