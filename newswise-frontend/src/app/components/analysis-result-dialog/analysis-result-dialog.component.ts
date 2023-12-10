import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA} from '@angular/material/dialog';
import {AnalysisResultData} from '../../model/analysis-result.data';
import {IMAGES_DIR} from '../../commons/app.constants';
import {DiseaseRisk} from '../../model/disease-risk.enum';
import {findKey} from 'lodash';

@Component({
    selector: 'app-analysis-result',
    templateUrl: './analysis-result-dialog.component.html',
    styleUrls: ['./analysis-result-dialog.component.scss']
})
export class AnalysisResultDialogComponent {
    readonly medicalRecordIcon = `${IMAGES_DIR}/medical-record.svg`;
    readonly headerIcon = `${IMAGES_DIR}/${this.data.disease ? 'sad' : 'happy'}.svg`;
    readonly riskLevel = findKey(DiseaseRisk, o => o === this.data.risk)!;

    constructor(@Inject(MAT_DIALOG_DATA) public data: AnalysisResultData) {
    }
}
