import {Pipe, PipeTransform} from '@angular/core';
import {map, Observable} from 'rxjs';

@Pipe({name: 'obsTransform'})
export class ObsTransformPipe implements PipeTransform {
    transform<T, R>(input: Observable<T>, apply: (input: T) => R): Observable<R> {
        return input.pipe(map(apply));
    }
}
