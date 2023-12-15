import {Component, ElementRef, EventEmitter, Input, Output, ViewChild} from '@angular/core';
import {FileUploadError} from '../../model/file-upload.error';

class FileValidationError extends Error {
    readonly filename: string | undefined;
    readonly error: FileUploadError;

    constructor(error: FileUploadError, filename: string | undefined = undefined) {
        super();
        this.filename = filename;
        this.error = error;
    }
}

@Component({
    selector: 'app-file-upload',
    templateUrl: './file-upload.component.html',
    styleUrl: './file-upload.component.scss'
})
export class FileUploadComponent {
    @Input() placeholder = 'Choose file';
    @Input() maxCount?: number;
    @Input() maxSizeKb?: number;
    @Input() mimeTypes = ['*'];
    @Output() fileEvent = new EventEmitter<File[]>();
    @Output() errorEvent = new EventEmitter<[FileUploadError, string?]>();

    @ViewChild('fileUpload') private fileUpload?: ElementRef<HTMLInputElement>;

    protected onFilesSelected = (event: Event) => {
        const files = this.extractFiles(event);
        try {
            const collectedFiles = Array.from(this.collectFiles(files));
            this.fileEvent.emit(collectedFiles);
        } catch (ex) {
            ex instanceof FileValidationError && this.errorEvent.emit([ex.error, ex.filename]);
        }
        this.fileUpload!.nativeElement.value = '';
    };

    private extractFiles = (event: Event) => {
        const target = event.target as HTMLInputElement;
        return target.files as FileList;
    };

    private* collectFiles(files: FileList) {
        if (this.maxCount && this.maxCount < files.length) {
            throw new FileValidationError(FileUploadError.MAX_FILE_COUNT_EXCEEDED);
        }
        for (const file of Array.from(files)) {
            if (!this.isMimeTypeValid(file)) {
                throw new FileValidationError(FileUploadError.UNSUPPORTED_TYPE, file.name);
            } else if (!this.isSizeValid(file)) {
                throw new FileValidationError(FileUploadError.FILE_SIZE_EXCEEDED, file.name);
            }
            yield file;
        }
    }

    private isSizeValid = (file: File) => !this.maxSizeKb || this.maxSizeKb * 1024 >= file.size;

    private isMimeTypeValid = (file: File) => this.mimeTypes.includes('*') || this.mimeTypes.includes(file.type);
}
