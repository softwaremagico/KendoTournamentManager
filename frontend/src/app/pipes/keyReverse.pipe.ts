import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
  name: 'keyReverse',
  standalone: true
})
export class KeyReversePipe implements PipeTransform {
  transform(value: IterableIterator<number>): IterableIterator<number> {
    return Array.from(value).reverse().values();
  }
}
