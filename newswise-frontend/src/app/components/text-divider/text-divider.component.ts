import {Component, Input} from '@angular/core';

@Component({
    selector: 'app-text-divider',
    templateUrl: './text-divider.component.html',
    styleUrls: ['./text-divider.component.scss']
})
export class TextDividerComponent {
    @Input() text?: string;
}
