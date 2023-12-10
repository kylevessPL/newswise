import {MissingTranslationHandler, MissingTranslationHandlerParams} from '@ngx-translate/core';

export class GlobalMissingTranslationHandler implements MissingTranslationHandler {
    handle(params: MissingTranslationHandlerParams) {
        return params.interpolateParams?.['default' as keyof unknown] || params.key;
    }
}
