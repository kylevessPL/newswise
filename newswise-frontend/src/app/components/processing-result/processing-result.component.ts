import {Component, Input} from '@angular/core';
import {DocumentProcessingData} from '../../model/document-processing-data';
import {DocumentProcessingFailure} from '../../model/document-processing-failure';
import {DocumentProcessingSuccess} from '../../model/document-processing-success';
import {MatDialog} from '@angular/material/dialog';
import {ProcessingDetailsDialogComponent} from '../processing-details-dialog/processing-details-dialog.component';
import {DocumentProcessingError} from '../../model/document-processing-error.enum';
import EnumUtil from '../../utils/enum.util';
import {Animations} from '../../commons/app.animations';
import DocumentUtil from '../../utils/document.util';
import {DocumentCategoryEnum} from '../../model/document-category.enum';

@Component({
    selector: 'app-processing-result',
    templateUrl: './processing-result.component.html',
    styleUrl: './processing-result.component.scss',
    animations: [Animations.displayState]
})
export class ProcessingResultComponent {
    @Input() documents: (DocumentProcessingData | null)[];

    constructor(private matDialog: MatDialog) {
    }

    protected get ready() {
        return this.documents.at(-1) !== null;
    }

    protected isProcessing = (document: DocumentProcessingData) =>
        !this.ready && !this.isSuccess(document) && !this.isFailure(document);

    protected isSuccess = (document: DocumentProcessingData): document is DocumentProcessingSuccess =>
        DocumentUtil.isSuccess(document);

    protected isFailure = (document: DocumentProcessingData): document is DocumentProcessingFailure =>
        DocumentUtil.isFailure(document);

    protected documentState = (document: DocumentProcessingData) => {
        if (this.isSuccess(document)) {
            return true;
        } else if (this.isFailure(document)) {
            return false;
        } else {
            return undefined;
        }
    };

    protected extractTopic = (document: DocumentProcessingSuccess) => {
        const prediction = Array.from(document.predictions.entries())
            .reduce((a, b) => b[1] > a[1] ? b : a)[0];
        return EnumUtil.getEnumValue(DocumentCategoryEnum, prediction)!!;
    };

    protected extractError = (document: DocumentProcessingFailure) =>
        EnumUtil.getEnumValue(DocumentProcessingError, document.errorMessage) ?? 'unexpected-error';

    protected openDetailsDialog = (document: DocumentProcessingData) =>
        this.matDialog.open(ProcessingDetailsDialogComponent, {data: document});
}
