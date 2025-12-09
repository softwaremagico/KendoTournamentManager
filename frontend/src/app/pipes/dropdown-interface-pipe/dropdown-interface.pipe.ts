import {Pipe, PipeTransform} from '@angular/core';

@Pipe({
  name: 'dropdownInterface',
  pure: false,
})
export class DropdownInterfacePipe implements PipeTransform {

  transform(value: string | undefined, data: { value: string, label: string }[]): { value: string, label: string } {
    if (value) {
      const result: any | undefined = data.find(item => item.value == value);
      if (result) {
        return result;
      }
    }
    return {value: '', label: ''};
  }
}
