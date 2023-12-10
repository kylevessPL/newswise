import {Pipe, PipeTransform} from '@angular/core';
import {Observable, skip} from 'rxjs';

@Pipe({name: 'obsSkipFirst'})
export class ObsSkipFirstPipe implements PipeTransform {
    transform<T>(input: Observable<T>, count = 1): Observable<T> {
        return input.pipe(skip(count));
    }
}
