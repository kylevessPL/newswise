import {Component, Input} from '@angular/core';

@Component({
    selector: 'app-document',
    templateUrl: './document.component.svg',
    styleUrl: './document.component.scss'
})
export class DocumentComponent {
    @Input() success?: boolean;
}
