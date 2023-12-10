import {Pipe, PipeTransform} from '@angular/core';
import {DomSanitizer, SafeUrl} from '@angular/platform-browser';

@Pipe({name: 'blobUrl'})
export class BlobUrlPipe implements PipeTransform {
    constructor(private sanitizer: DomSanitizer) {
    }

    transform(input?: Blob): SafeUrl | undefined {
        return input ? this.sanitizer.bypassSecurityTrustUrl(URL.createObjectURL(input)) : undefined;
    }
}
