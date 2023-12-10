import {Component} from '@angular/core';
import {IMAGES_DIR} from '../../commons/app.constants';

@Component({
    selector: 'app-footer',
    templateUrl: './footer.component.html',
    styleUrls: ['./footer.component.scss']
})
export class FooterComponent {
    readonly logoIcon = `${IMAGES_DIR}/footer.svg`;
    readonly year = new Date().getFullYear();
}
