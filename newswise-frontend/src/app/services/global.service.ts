import {Subject} from 'rxjs';
import {HttpErrorResponse} from '@angular/common/http';
import {Injectable} from '@angular/core';

@Injectable({providedIn: 'root'})
export class GlobalService {

    httpError = new Subject<HttpErrorResponse>;
}
