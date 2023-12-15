import {Injectable} from '@angular/core';
import {MatIconRegistry} from '@angular/material/icon';
import {DomSanitizer} from '@angular/platform-browser';
import {IMAGES_DIR} from '../commons/app.constants';

@Injectable({providedIn: 'root'})
export class IconsService {
    constructor(private iconRegistry: MatIconRegistry, private sanitizer: DomSanitizer) {
    }

    initialize = async () => this.setupIcons();

    private setupIcons() {
        this.iconRegistry.addSvgIcon('en', this.sanitizer.bypassSecurityTrustResourceUrl(`${IMAGES_DIR}/en.svg`));
    }
}
