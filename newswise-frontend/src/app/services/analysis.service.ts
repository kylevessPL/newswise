import {Injectable} from '@angular/core';
import {HttpClient} from '@angular/common/http';
import {AnalysisResultData} from '../model/analysis-result.data';
import {environment} from '../../environments/environment';
import {restUrl} from '../../environments/rest-url';
import {AnalysisExportPeriodData} from '../model/analysis-export-period.data';
import HttpUtil from '../utils/http.util';
import {first, shareReplay} from 'rxjs';
import {PreliminaryStudyData} from '../model/preliminary-study.data';

@Injectable({providedIn: 'root'})
export class AnalysisService {
    constructor(private httpClient: HttpClient) {
    }

    getAllExportPeriods = () => this.httpClient.get<AnalysisExportPeriodData[]>(
        `${environment.apiUrl}/${restUrl.files}/${restUrl.remote}`
    ).pipe(shareReplay(1));

    export(period: AnalysisExportPeriodData) {
        this.httpClient
            .get<ArrayBuffer>(`${environment.apiUrl}/${restUrl.files}`, {
                params: {period: period.key},
                responseType: 'ArrayBuffer' as 'json',
                observe: 'response'
            })
            .pipe(first())
            .subscribe(({body, headers}) => HttpUtil.downloadFile(body!, headers));
    }

    analyse(audio: Blob, preliminaryStudy?: PreliminaryStudyData) {
        const formData = new FormData();
        formData.append('file', audio);
        preliminaryStudy && formData.append('data', JSON.stringify(preliminaryStudy));
        return this.httpClient.post<AnalysisResultData>(`${environment.apiUrl}/${restUrl.remote}`, formData);
    }
}
