import {Injectable} from '@angular/core';
import {TranslateService} from '@ngx-translate/core';
import {SUPPORTED_LANGUAGES} from '../commons/app.constants';
import {Language} from '../model/language.enum';
import {firstValueFrom} from 'rxjs';

@Injectable({providedIn: 'root'})
export class LocalizationService {
    constructor(private translateService: TranslateService) {
    }

    async initialize() {
        this.translateService.addLangs(SUPPORTED_LANGUAGES);
    }

    getSupportedLanguages = () => this.translateService.langs
        .map(code => Language.getByCode(code))
        .filter((language): language is Language => !!language);

    getLanguage = () => Language.getByCode(this.translateService.currentLang) ?? Language.ENGLISH;

    setLanguage(language: Language) {
        if (!this.translateService.langs.includes(language.code)) {
            throw new Error(`Language ${language} is not supported!`);
        }
        this.translateService.use(language.code);
    }

    translate = (key: string, params?: { [key: string]: any }): Promise<string> =>
        firstValueFrom(this.translateService.get(key, params));
}
