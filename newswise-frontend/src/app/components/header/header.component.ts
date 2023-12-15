import {Component} from '@angular/core';
import {LocalizationService} from '../../services/localization.service';
import {FormBuilder, FormGroup} from '@angular/forms';
import {Language} from '../../model/language.enum';

@Component({
    selector: 'app-header',
    templateUrl: './header.component.html',
    styleUrl: './header.component.scss'
})
export class HeaderComponent {
    protected readonly languages = this.localizationService.getSupportedLanguages();
    protected currentLanguage = this.localizationService.getLanguage();

    protected form: FormGroup;

    constructor(private localizationService: LocalizationService, private formBuilder: FormBuilder) {
        this.form = this.formBuilder.group({
            language: this.currentLanguage
        });
    }

    protected onLanguageChange = (language: Language) => {
        this.currentLanguage = language;
        this.localizationService.setLanguage(language);
    };
}
