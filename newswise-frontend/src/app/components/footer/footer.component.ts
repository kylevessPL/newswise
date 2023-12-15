import {Component} from '@angular/core';
import {IMAGES_DIR} from '../../commons/app.constants';

@Component({
    selector: 'app-footer',
    templateUrl: './footer.component.html',
    styleUrl: './footer.component.scss'
})
export class FooterComponent {
    protected readonly logoIcon = `${IMAGES_DIR}/footer.svg`;
    protected readonly year = new Date().getFullYear();
}
