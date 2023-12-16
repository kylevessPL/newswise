import {Pipe, PipeTransform} from '@angular/core';

@Pipe({name: 'filterPipe', pure: false})
export class FilterPipe implements PipeTransform {
    transform<T>(items: T[], filter?: T): any[] {
        return items.filter(item => item !== filter);
    }
}
