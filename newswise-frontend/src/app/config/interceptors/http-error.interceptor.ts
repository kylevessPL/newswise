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
                if (error.error instanceof ErrorEvent) {
                    console.error(`An HTTP error occurred: ${error.error.message}`);
                } else {
                    console.error(`Backend returned error code: ${error.status}\nMessage: ${error.message}`);
                }
                this.globalService.httpError.next(error);
                return throwError(() => error);
            })
        );
    }
}
