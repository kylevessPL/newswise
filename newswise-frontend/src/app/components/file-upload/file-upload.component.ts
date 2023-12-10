import {Component, ElementRef, EventEmitter, Input, OnDestroy, OnInit, Output, ViewChild} from '@angular/core';
import {FileUploadError} from '../../model/file-upload.error';
import AudioUtil from '../../utils/audio.util';
import {Observable, Subscription} from 'rxjs';

@Component({
    selector: 'app-file-upload',
    templateUrl: './file-upload.component.html',
    styleUrls: ['./file-upload.component.scss']
})
export class FileUploadComponent implements OnInit, OnDestroy {
    @Input() placeholder?: string;
    @Input() minDuration?: number;
    @Input() maxDuration?: number;
    @Input() maxSizeKb?: number;
    @Input() mimeTypes = ['*'];
    @Input() externalChange?: Observable<boolean>;
    @Output() fileEvent = new EventEmitter<File>();
    @Output() errorEvent = new EventEmitter<FileUploadError>();

    fileName?: string;

    @ViewChild('fileUpload') private fileUpload?: ElementRef<HTMLInputElement>;
    private externalChangeSubscription?: Subscription;

    ngOnInit() {
        this.externalChangeSubscription = this.externalChange?.subscribe(changed => this.handleExternalChange(changed));
    }

    ngOnDestroy = () => this.externalChangeSubscription?.unsubscribe();

    onFileSelected(event: Event) {
        const file = this.extractFile(event);
        file && this.validateFile(file).then(() => {
            this.fileName = file.name;
            this.fileEvent.emit(file);
        }).catch(error => {
            this.clearSelection();
            this.fileEvent.emit(undefined);
            this.errorEvent.emit(error);
        });
    }

    private handleExternalChange(changed: boolean) {
        if (changed) {
            this.clearSelection();
        }
    }

    private clearSelection() {
        this.fileUpload!.nativeElement.value = '';
        this.fileName = undefined;
    }

    private extractFile(event: Event) {
        const target = event.target as HTMLInputElement;
        return (target.files as FileList)[0];
    }

    private async validateFile(file: File) {
        if (!this.isMimeTypeValid(file)) {
            return Promise.reject(FileUploadError.INVALID_TYPE);
        } else if (!this.isSizeValid(file)) {
            return Promise.reject(FileUploadError.FILE_SIZE_EXCEEDED);
        } else if (!await this.isDurationValid(file)) {
            return Promise.reject(FileUploadError.DURATION_EXCEEDED);
        }
        return Promise.resolve();
    }

    private isSizeValid = (file: File) => !this.maxSizeKb || this.maxSizeKb * 1024 >= file.size;

    private isMimeTypeValid = (file: File) => this.mimeTypes.includes('*') || this.mimeTypes.includes(file.type);

    private async isDurationValid(file: File) {
        const audioBuffer = await AudioUtil.blobToAudioBuffer(file);
        const minDurationValid = !this.minDuration || this.minDuration <= audioBuffer.duration;
        const maxDurationValid = !this.maxDuration || this.maxDuration >= audioBuffer.duration;
        return minDurationValid && maxDurationValid;
    }
}
