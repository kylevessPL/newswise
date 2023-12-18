import {Component, Input, OnChanges} from '@angular/core';
import {IMAGES_DIR} from '../../commons/app.constants';

@Component({
    selector: 'app-document',
    templateUrl: './document.component.html',
    styleUrl: './document.component.scss'
})
export class DocumentComponent implements OnChanges {
    @Input() success?: boolean;

    private date: Date;

    ngOnChanges() {
        this.date = new Date();
    }

    protected get icon() {
        if (this.success) {
            return `${IMAGES_DIR}/document-success.svg?${this.date.getTime()}`;
        } else if (this.success === false) {
            return `${IMAGES_DIR}/document-failure.svg?${this.date.getTime()}`;
        } else {
            return `${IMAGES_DIR}/document.svg`;
        }
    };
}
