import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {environment} from '../../environments/environment';
import {restUrl} from '../../environments/rest-url';
import {defer, finalize, first, map, Observable, Subject} from 'rxjs';
import {DocumentProcessingSuccess} from '../model/document-processing-success';
import {DocumentProcessingData} from '../model/document-processing-data';
import {fetchEventSource} from '@microsoft/fetch-event-source';
import {EventEnum} from '../model/event.enum';
import {DocumentProcessingFailure} from '../model/document-processing-failure';
import {ModelEnum} from '../model/model.enum';
import DocumentUtil from '../utils/document.util';

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
        }).pipe(map(this.mapResponse), first());

    processFiles = (model: ModelEnum, files: File[]) => {
        const data = files.reduce((formData, file, idx) => {
            formData.append(`${idx}`, file);
            return formData;
        }, new FormData());
        return this.processFileEvents(model, data);
    };

    private processFileEvents = (model: ModelEnum, data: FormData): Observable<DocumentProcessingData> => defer(() => {
        const results = new Subject<DocumentProcessingData>();
        let controller = new AbortController();
        fetchEventSource(`${environment.apiUrl}/${restUrl.processing}/${model}/${restUrl.files}`, {
            method: 'POST',
            headers: {
                'Accept': '*/*'
            },
            body: data,
            signal: controller.signal,
            openWhenHidden: true,
            async onopen(response) {
                if (!response.ok || response.headers.get('content-type') !== 'text/event-stream;charset=UTF-8') {
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
                console.error(err);
                results.error(err);
                throw err;
            }
        }).then();
        return results.pipe(map(this.mapResponse), finalize(() => controller.abort()));
    });

    private mapResponse = (x: DocumentProcessingData) => ({
        ...x,
        metadata: this.isSuccess(x) ? this.createMap(x.metadata) : undefined,
        predictions: this.isSuccess(x) ? this.createMap(x.predictions) : undefined
    } as DocumentProcessingSuccess);


    private createMap = (data: Map<string, number>) => data !== undefined
        ? new Map(Object.entries(data))
        : data;

    private isSuccess = (document: DocumentProcessingData): document is DocumentProcessingSuccess =>
        DocumentUtil.isSuccess(document);
}
