import {AbstractControl, AsyncValidatorFn, ValidationErrors} from '@angular/forms';
import {from, map, Observable, of, take} from 'rxjs';
import isReachable from 'reachy-url';
import {catchError} from 'rxjs/operators';

export default class ValidationUtil {
    static urlValidator = (): AsyncValidatorFn => (control: AbstractControl): Observable<ValidationErrors | null> =>
        from(isReachable(control.value)).pipe(
            map(() => null),
            catchError(() => of({invalidUrl: control.value})),
            take(1)
        );
}
