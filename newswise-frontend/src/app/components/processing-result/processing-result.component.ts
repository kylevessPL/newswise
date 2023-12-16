import {Component, Input} from '@angular/core';
import {DocumentProcessingData} from '../../model/document-processing-data';
import {DocumentProcessingFailure} from '../../model/document-processing-failure';
import {DocumentProcessingSuccess} from '../../model/document-processing-success';
import {ANIMATIONS_DIR, IMAGES_DIR} from '../../commons/app.constants';
import {MatDialog} from '@angular/material/dialog';
import {ProcessingDetailsDialogComponent} from '../processing-details-dialog/processing-details-dialog.component';
import {DocumentProcessingError} from '../../model/document-processing-error.enum';
import EnumUtil from '../../utils/enum.util';
import {Animations} from '../../commons/app.animations';
import DocumentUtil from '../../utils/document.util';

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

    protected documentIcon = (document: DocumentProcessingData) => {
        if (this.ready) {
            return `${IMAGES_DIR}/document-ready.svg`;
        } else if (this.isProcessing(document)) {
            return `${ANIMATIONS_DIR}/document-processing.json`;
        } else if (this.isSuccess(document)) {
            return `${IMAGES_DIR}/document-success.svg`;
        } else {
            return `${IMAGES_DIR}/document-failure.svg`;
        }
    };

    protected extractTopic = (document: DocumentProcessingSuccess) => Array.from(document.predictions.entries())
        .reduce((a, b) => b[1] > a[1] ? b : a)[0];

    protected extractError = (document: DocumentProcessingFailure): string =>
        EnumUtil.getEnumValue(DocumentProcessingError, document.errorMessage) ?? 'unexpected-error';

    protected openDetailsDialog = (document: DocumentProcessingData) =>
        this.matDialog.open(ProcessingDetailsDialogComponent, {data: document});
}
