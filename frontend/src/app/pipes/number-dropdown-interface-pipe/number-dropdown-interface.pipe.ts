import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
  name: 'numberDropdownInterface',
  pure: false,
})
export class NumberDropdownInterfacePipe implements PipeTransform {

  transform(value: number, data: { value: number, label: string }[]): { value: number, label: string } {
    const result: any | undefined = data.find(item => item.value == value);
    if (result) {
      return result;
    }
    return {value: 0, label: ''};
  }
}
