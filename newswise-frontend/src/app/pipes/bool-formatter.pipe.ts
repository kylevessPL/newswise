import {Pipe, PipeTransform} from '@angular/core';

@Pipe({name: 'boolFormatterPipe'})
export class BoolFormatterPipe implements PipeTransform {
    transform(value: string): string {
        const bool = value.toLowerCase();
        if (bool === 'true') {
            return 'Yes';
        } else if (bool === 'false') {
            return 'No';
        } else {
            return value;
        }
    }
}
