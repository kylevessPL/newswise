import {Component, OnDestroy, OnInit} from '@angular/core';
import {MAX_AUDIO_DURATION, MAX_FILE_SIZE_KB, MIN_AUDIO_DURATION} from './commons/app.constants';
import {FileUploadError} from './model/file-upload.error';
import {MatSnackBar} from '@angular/material/snack-bar';
import UnitUtil from './utils/unit.util';
import {AudioRecordError} from './model/audio-record.error';
import {MimeType} from './model/mime-type.enum';
import {Animations} from './commons/app.animations';
import {MatDialog} from '@angular/material/dialog';
import {AnalysisResultData} from './model/analysis-result.data';
import {AnalysisResultDialogComponent} from './components/analysis-result-dialog/analysis-result-dialog.component';
import {AnalysisService} from './services/analysis.service';
import {BehaviorSubject, first, Observable, Subscription} from 'rxjs';
import {LocalizationService} from './services/localization.service';
import {AnalysisExportPeriodData} from './model/analysis-export-period.data';
import {AnalysisExportDialogComponent} from './components/analysis-export-dialog/analysis-export-dialog.component';
import {GlobalService} from './services/global.service';
import {ConfirmationDialogComponent} from './components/confirmation-dialog/confirmation-dialog.component';
import {ConfirmationData} from './model/confirmation.data';
import {PreliminaryStudyData} from './model/preliminary-study.data';
import {PreliminaryStudyDialogComponent} from './components/preliminary-study-dialog/preliminary-study-dialog.component';

@Component({
    selector: 'app-root',
    templateUrl: './app.component.html',
    styleUrls: ['./app.component.scss'],
    animations: [Animations.displayState]
})
export class AppComponent implements OnInit, OnDestroy {
    minAudioDuration = MIN_AUDIO_DURATION;
    maxAudioDuration = MAX_AUDIO_DURATION;
    maxAudioFileSize = MAX_FILE_SIZE_KB;
    acceptedAudioFileTypes = [MimeType.AUDIO_WAV, MimeType.AUDIO_MPEG, MimeType.AUDIO_OGG];
    processing = false;
    footerWithinViewport = true;
    audio: BehaviorSubject<Blob | undefined> = new BehaviorSubject<Blob | undefined>(undefined);
    httpErrorSubscription?: Subscription;
    analysisExportPeriods?: Observable<AnalysisExportPeriodData[]>;

    constructor(private analysisService: AnalysisService,
                private localizationService: LocalizationService,
                private globalService: GlobalService,
                private snackBar: MatSnackBar,
                private dialog: MatDialog) {
    }

    ngOnInit() {
        this.httpErrorSubscription = this.globalService.httpError.subscribe(() => this.handleHttpError());
        this.analysisExportPeriods = this.analysisService.getAllExportPeriods();
    }

    ngOnDestroy() {
        this.httpErrorSubscription?.unsubscribe();
    }

    onAnalyse = () => this.showPreliminaryStudyConfirmationDialog();

    onAudioLoaded = (audio?: Blob) => this.audio.next(audio);

    async onFileSelectError(error?: FileUploadError) {
        const message = await (async () => {
            switch (error) {
                case FileUploadError.INVALID_TYPE:
                    return await this.localizationService.translate('invalid-file-type-supported-are', {
                        supportedTypes: this.acceptedAudioFileTypes.map(({extension}) => extension)
                    });
                case FileUploadError.DURATION_EXCEEDED:
                    return await this.localizationService.translate('audio-duration-must-be-between-and-seconds-long', {
                        minAudioDuration: this.minAudioDuration,
                        maxAudioDuration: this.maxAudioDuration,
                    });
                case FileUploadError.FILE_SIZE_EXCEEDED:
                    return await this.localizationService.translate('file-is-too-big-maximum-allowed-size-is', {
                        maxAudioFileSize: UnitUtil.kilobytesPrettyPrint(this.maxAudioFileSize)
                    });
                default:
                    return await this.localizationService.translate('an-error-occurred-during-file-upload');
            }
        })();
        this.snackBar.open(message, 'OK');
    }

    async onRecordError(error?: AudioRecordError) {
        const key = error === AudioRecordError.PERMISSIONS
            ? 'insufficient-microphone-permissions'
            : 'an-error-occurred-during-audio-recording';
        const message = await this.localizationService.translate(key);
        this.snackBar.open(message, 'OK');
    }

    async onPlayerError() {
        const message = await this.localizationService.translate('audio-player-is-not-supported-by-the-browser');
        this.snackBar.open(message, 'OK');
    }

    onFooterWithinViewport(withinViewport: boolean) {
        this.footerWithinViewport = withinViewport;
    }

    instanceOfFile = (blob?: Blob) => blob instanceof File;

    notInstanceOfFile = (blob?: Blob) => !this.instanceOfFile(blob);

    showAnalysisExportDialog() {
        const dialogRef = this.dialog.open(AnalysisExportDialogComponent, {
            panelClass: 'analysis-export-dialog',
            data: this.analysisExportPeriods
        });
        dialogRef.afterClosed()
            .pipe(first())
            .subscribe((period?: AnalysisExportPeriodData) => period && this.analysisService.export(period));
    }

    private showPreliminaryStudyConfirmationDialog() {
        const dialogRef = this.dialog.open(ConfirmationDialogComponent, {
            data: {
                title: 'preliminary-study',
                message: 'preliminary-study-question',
            } as ConfirmationData
        });
        dialogRef.afterClosed()
            .pipe(first())
            .subscribe((res: boolean) => res ? this.showPreliminaryStudyDialog() : this.process());
    }

    private showPreliminaryStudyDialog() {
        const dialogRef = this.dialog.open(PreliminaryStudyDialogComponent, {
            panelClass: 'preliminary-study-dialog',
            disableClose: true,
            data: this.localizationService.getLanguage()
        });
        dialogRef.afterClosed()
            .pipe(first())
            .subscribe((data: PreliminaryStudyData) => this.process(data));
    }

    private showAnalysisResultDialog(result: AnalysisResultData) {
        this.dialog.open(AnalysisResultDialogComponent, {
            panelClass: 'analysis-result-dialog',
            data: result
        });
    }

    private process(preliminaryStudy?: PreliminaryStudyData) {
        this.processing = true;
        this.analysisService.analyse(this.audio.value!, preliminaryStudy)
            .pipe(first())
            .subscribe({
                next: result => this.showAnalysisResultDialog(result),
                complete: () => {
                    this.processing = false;
                },
                error: () => {
                    this.processing = false;
                }
            });
    }

    private async handleHttpError() {
        const message = await this.localizationService.translate('sorry-something-went-wrong');
        this.snackBar.open(message, 'OK');
    }
}
