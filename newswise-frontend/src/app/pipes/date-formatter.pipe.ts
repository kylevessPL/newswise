import {Pipe, PipeTransform} from '@angular/core';
import {DATETIME_FORMAT} from '../commons/app.constants';
import moment from 'moment';

@Pipe({name: 'dateFormatterPipe'})
export class DateFormatterPipe implements PipeTransform {
    transform(value: string): string {
        const date = moment(value);
        return date.isValid() ? date.format(DATETIME_FORMAT) : value;
    }
}
