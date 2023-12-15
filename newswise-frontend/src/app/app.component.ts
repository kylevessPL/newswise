import {Component, OnDestroy, OnInit} from '@angular/core';
import {ACCEPTED_MIME_TYPES, MAX_FILE_COUNT, MAX_FILE_SIZE_MB} from './commons/app.constants';
import {FileUploadError} from './model/file-upload.error';
import {MatSnackBar} from '@angular/material/snack-bar';
import UnitUtil from './utils/unit.util';
import {Animations} from './commons/app.animations';
import {ProcessingService} from './services/processing.service';
import {Observable, Subscription} from 'rxjs';
import {LocalizationService} from './services/localization.service';
import {GlobalService} from './services/global.service';
import {DocumentProcessingData} from './model/document-processing-data';
import {DocumentProcessingFailure} from './model/document-processing-failure';
import {HttpErrorResponse} from '@angular/common/http';
import {ModelEnum} from './model/model.enum';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrl: './app.component.scss',
    animations: [Animations.displayState]
})
export class AppComponent implements OnInit, OnDestroy {
    protected readonly maxFileCount = MAX_FILE_COUNT;
    protected readonly maxFileSize = MAX_FILE_SIZE_MB;
    protected readonly acceptedFileMimeTypes = ACCEPTED_MIME_TYPES;

    protected documentProcessingData: (DocumentProcessingData | null)[] = [];
    protected processCall?: (mode: ModelEnum) => Observable<DocumentProcessingData>;
    protected model: ModelEnum;

    private httpErrorSubscription?: Subscription;

    constructor(private processingService: ProcessingService,
                private localizationService: LocalizationService,
                private globalService: GlobalService,
                private snackBar: MatSnackBar) {
    }

    ngOnInit() {
        this.httpErrorSubscription = this.globalService.httpError.subscribe(() => this.handleHttpError());
    }

    ngOnDestroy() {
        this.httpErrorSubscription?.unsubscribe();
    }

    protected onFilesLoaded = async (files: File[]) => {
        this.documentProcessingData = files.map(file => this.initDocumentProcessingData(file.name));
        this.processCall = model => this.processingService.processFiles(model, files);
    };

    protected onUrlLoaded = (url: URL) => {
        this.documentProcessingData = [this.initDocumentProcessingData(url)];
        this.processCall = model => this.processingService.processRemote(model, url);
    };

    protected onModelSelected = (model: ModelEnum) => {
        this.model = model;
    };

    protected process = () => {
        if (!this.documentProcessingData?.filter(x => x !== null)?.length || !this.processCall) {
            return;
        }
        this.documentProcessingData.push(null);
        const urlDocument = typeof this.documentProcessingData.at(0)?.resource === 'string';
        this.processCall(this.model).subscribe({
            next: data => {
                const doc = this.documentProcessingData.at(urlDocument ? 0 : Number(data.name))!!;
                Object.assign(doc, data);
            },
            error: (error?: HttpErrorResponse) => {
                if (!urlDocument) {
                    this.handleHttpError();
                } else if (error?.status === 400) {
                    const doc = this.documentProcessingData[0] as DocumentProcessingFailure;
                    doc.errorMessage = JSON.parse(error?.error).message;
                }
            },
            complete: () => {
                this.processCall = undefined;
            }
        });
    };

    protected onFileSelectError = async (selectError: [FileUploadError, string?]) => {
        const [error, filename] = selectError;
        const message = (async () => {
            switch (error) {
                case FileUploadError.MAX_FILE_COUNT_EXCEEDED:
                    return await this.localizationService.translate('maximum-file-count-exceeded', {
                        count: this.maxFileCount
                    });
                case FileUploadError.UNSUPPORTED_TYPE:
                    return await this.localizationService.translate('unsupported-file-type', {
                        filename
                    });
                case FileUploadError.FILE_SIZE_EXCEEDED:
                    return await this.localizationService.translate('max-file-size-exceeded', {
                        filename,
                        maxAudioFileSize: UnitUtil.kilobytesPrettyPrint(this.maxFileSize)
                    });
                default:
                    return await this.localizationService.translate('sorry-something-went-wrong');
            }
        })();
        this.snackBar.open(await message, 'OK');
    };

    private initDocumentProcessingData = (filename?: string | URL) => ({resource: filename} as DocumentProcessingData);

    private handleHttpError = () => {
        (async () => {
            const message = await this.localizationService.translate('sorry-something-went-wrong');
            this.snackBar.open(message, 'OK');
        })();
    };
}
