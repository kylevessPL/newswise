import {Component, ElementRef, Inject, ViewChild} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {Model, SurveyModel} from 'survey-core';
import preliminaryStudy from '../../../assets/surveys/preliminary-study.json';
import {Language} from '../../model/language.enum';
import {CompletingEvent} from 'survey-core/typings/survey-events-api';

@Component({
    selector: 'app-preliminary-study',
    templateUrl: './preliminary-study-dialog.component.html',
    styleUrls: ['./preliminary-study-dialog.component.scss']
})
export class PreliminaryStudyDialogComponent {
    survey: Model;

    @ViewChild('completeBtn') private completeBtn?: ElementRef<HTMLInputElement>;

    constructor(@Inject(MAT_DIALOG_DATA) public language: Language,
                private dialogRef: MatDialogRef<PreliminaryStudyDialogComponent>) {
        this.survey = new Model(preliminaryStudy);
        this.survey.locale = language.code;
        this.survey.onCompleting.add(this.onSurveyCompleting);
    }

    public onSurveyCompleted = () => this.dialogRef.close(this.survey.data);

    private onSurveyCompleting = (sender: SurveyModel, event: CompletingEvent) => {
        event.allow = false;
        this.completeBtn?.nativeElement.click();
    };
}
