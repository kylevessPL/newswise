import {Component} from '@angular/core';
import {LocalizationService} from '../../services/localization.service';
import {FormBuilder, FormGroup} from '@angular/forms';
import {Language} from '../../model/language.enum';

@Component({
    selector: 'app-header',
    templateUrl: './header.component.html',
    styleUrls: ['./header.component.scss']
})
export class HeaderComponent {
    readonly languages = this.localizationService.getSupportedLanguages();
    currentLanguage = this.localizationService.getLanguage();

    form: FormGroup;

    constructor(private localizationService: LocalizationService, private formBuilder: FormBuilder) {
        this.form = this.formBuilder.group({
            language: this.currentLanguage
        });
    }

    onLanguageChange(language: Language) {
        this.currentLanguage = language;
        this.localizationService.setLanguage(language);
    }
}
