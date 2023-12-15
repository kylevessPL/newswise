import {HttpErrorResponse, HttpHandler, HttpInterceptor, HttpRequest} from '@angular/common/http';
import {throwError} from 'rxjs';
import {catchError} from 'rxjs/operators';
import {Injectable} from '@angular/core';
import {GlobalService} from '../../services/global.service';

@Injectable()
export class HttpErrorInterceptor implements HttpInterceptor {
    constructor(private globalService: GlobalService) {
    }

    intercept(request: HttpRequest<any>, next: HttpHandler) {
        return next.handle(request).pipe(
            catchError((error: HttpErrorResponse) => {
                if (!(error.status >= 400 && error.status < 500)) {
                    this.globalService.httpError.next(error);
                }
                console.error(error);
                return throwError(() => error);
            })
        );
    }
}
