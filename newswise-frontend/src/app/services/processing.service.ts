import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../environments/environment';
import {restUrl} from '../../environments/rest-url';
import {defer, finalize, first, Observable, Subject} from 'rxjs';
import {DocumentProcessingSuccess} from '../model/document-processing-success';
import {DocumentProcessingData} from '../model/document-processing-data';
import {EventStreamContentType, fetchEventSource} from '@microsoft/fetch-event-source';
import {EventEnum} from '../model/event.enum';
import {DocumentProcessingFailure} from '../model/document-processing-failure';
import {ModelEnum} from '../model/model.enum';

class ClosedConnectionError extends Error {
    constructor() {
        super('Server closed connection prematurely');
    }
}

class ResponseError extends Error {
    readonly response: Response;

    constructor(response: Response) {
        super(response.statusText);
        this.response = response;
    }
}

@Injectable({providedIn: 'root'})
export class ProcessingService {
    constructor(private httpClient: HttpClient) {
    }

    processRemote = (model: ModelEnum, url: URL) => this.httpClient
        .get<DocumentProcessingSuccess>(`${environment.apiUrl}/${restUrl.processing}/${model}/${restUrl.remote}`, {
            params: {url: encodeURI(url.toString())}
        }).pipe(first());

    processFiles = (model: ModelEnum, files: File[]) => {
        const data = files.reduce((formData, file, idx) => {
            formData.append(`${idx}`, file);
            return formData;
        }, new FormData());
        return this.processFileEvents(model, data);
    };

    private processFileEvents = (model: ModelEnum, data: FormData): Observable<DocumentProcessingData> => defer(() => {
        const results = new Subject<DocumentProcessingData>();
        const controller = new AbortController();
        fetchEventSource(`${environment.apiUrl}/${restUrl.processing}/${model}/${restUrl.files}`, {
            method: 'POST',
            headers: {
                'Content-Type': 'multipart/form-data',
            },
            body: data,
            signal: controller.signal,
            async onopen(response) {
                if (!response.ok || response.headers.get('content-type') !== EventStreamContentType) {
                    throw new ResponseError(response);
                }
            },
            onmessage(message) {
                if (message.event === EventEnum.END) {
                    results.complete();
                } else if (message.event !== EventEnum.START) {
                    const data = JSON.parse(message.data);
                    if (!data.hasOwnProperty('errorMessage')) {
                        results.next(data as DocumentProcessingSuccess);
                    } else {
                        results.next(data as DocumentProcessingFailure);
                    }
                }
            },
            onclose() {
                throw new ClosedConnectionError();
            },
            onerror(err) {
                console.error('Error processing files');
                if (err instanceof ResponseError) {
                    console.error(err.response);
                } else if (err instanceof Error) {
                    console.error(err);
                }
                results.error(err);
            }
        }).then();
        return results.pipe(finalize(() => controller.abort()));
    });
}
