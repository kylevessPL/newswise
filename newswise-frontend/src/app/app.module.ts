import {APP_INITIALIZER, NgModule} from '@angular/core';
import {BrowserModule} from '@angular/platform-browser';
import {AppComponent} from './app.component';
import {HeaderComponent} from './components/header/header.component';
import {BrowserAnimationsModule} from '@angular/platform-browser/animations';
import {MatButtonModule} from '@angular/material/button';
import {MatIconModule} from '@angular/material/icon';
import {MatDividerModule} from '@angular/material/divider';
import {MatCardModule} from '@angular/material/card';
import {TextDividerComponent} from './components/text-divider/text-divider.component';
import {FileUploadComponent} from './components/file-upload/file-upload.component';
import {MAT_SNACK_BAR_DEFAULT_OPTIONS, MatSnackBarModule} from '@angular/material/snack-bar';
import {ResourcePipe} from './pipes/resource.pipe';
import {MAT_DIALOG_DEFAULT_OPTIONS, MatDialogModule} from '@angular/material/dialog';
import {DatePipe, NgOptimizedImage} from '@angular/common';
import {HTTP_INTERCEPTORS, HttpClient, HttpClientModule} from '@angular/common/http';
import {HttpErrorInterceptor} from './config/interceptors/http-error.interceptor';
import {MissingTranslationHandler, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {TranslateHttpLoader} from '@ngx-translate/http-loader';
import {FooterComponent} from './components/footer/footer.component';
import {MAT_FORM_FIELD_DEFAULT_OPTIONS, MatFormFieldModule} from '@angular/material/form-field';
import {MatSelectModule} from '@angular/material/select';
import {ReactiveFormsModule} from '@angular/forms';
import {LocalizationService} from './services/localization.service';
import {IconsService} from './services/icons.service';
import {GlobalMissingTranslationHandler} from './config/handlers/global-missing-translation.handler';
import {UrlInputComponent} from './components/url-input/url-input.component';
import {MatInputModule} from '@angular/material/input';
import {MatProgressSpinnerModule} from '@angular/material/progress-spinner';
import {MatChipsModule} from '@angular/material/chips';
import {ModelSelectorComponent} from './components/model-selector/model-selector.component';
import {ProcessingResultComponent} from './components/processing-result/processing-result.component';
import {ProcessingDetailsDialogComponent} from './components/processing-details-dialog/processing-details-dialog.component';
import {MatExpansionModule} from '@angular/material/expansion';
import {MatListModule} from '@angular/material/list';
import {DateFormatterPipe} from './pipes/date-formatter.pipe';
import {FilterPipe} from './pipes/filter.pipe';
import {MatTooltipModule} from '@angular/material/tooltip';
import {SmoothHeightDirective} from './directives/smooth-height.directive';
import {DocumentComponent} from './components/document/document.component';
import {MatGridListModule} from '@angular/material/grid-list';

export function iconsInitializerFactory(iconsService: IconsService) {
    return () => iconsService.initialize();
}

export function localizationInitializerFactory(localizationService: LocalizationService) {
    return () => localizationService.initialize();
}

export function httpLoaderFactory(http: HttpClient) {
    return new TranslateHttpLoader(http);
}

@NgModule({
    declarations: [
        DateFormatterPipe,
        ResourcePipe,
        FilterPipe,
        AppComponent,
        FooterComponent,
        HeaderComponent,
        TextDividerComponent,
        UrlInputComponent,
        FileUploadComponent,
        DocumentComponent,
        SmoothHeightDirective,
        ProcessingDetailsDialogComponent,
        ModelSelectorComponent,
        ProcessingResultComponent
    ],
    imports: [
        BrowserModule,
        BrowserAnimationsModule,
        MatButtonModule,
        MatIconModule,
        MatDividerModule,
        MatCardModule,
        MatDialogModule,
        MatSnackBarModule,
        HttpClientModule,
        NgOptimizedImage,
        TranslateModule.forRoot({
            defaultLanguage: 'en',
            missingTranslationHandler: {
                provide: MissingTranslationHandler,
                useClass: GlobalMissingTranslationHandler
            },
            loader: {
                provide: TranslateLoader,
                useFactory: httpLoaderFactory,
                deps: [HttpClient]
            }
        }),
        MatFormFieldModule,
        MatSelectModule,
        ReactiveFormsModule,
        MatInputModule,
        MatProgressSpinnerModule,
        MatChipsModule,
        MatExpansionModule,
        MatListModule,
        MatTooltipModule,
        MatGridListModule
    ],
    providers: [
        DatePipe,
        {
            provide: APP_INITIALIZER,
            useFactory: localizationInitializerFactory,
            deps: [LocalizationService],
            multi: true
        },
        {
            provide: APP_INITIALIZER,
            useFactory: iconsInitializerFactory,
            deps: [IconsService],
            multi: true
        },
        {
            provide: HTTP_INTERCEPTORS,
            useClass: HttpErrorInterceptor,
            multi: true
        },
        {
            provide: MAT_FORM_FIELD_DEFAULT_OPTIONS,
            useValue: {
                appearance: 'outline'
            }
        },
        {
            provide: MAT_DIALOG_DEFAULT_OPTIONS,
            useValue: {
                autoFocus: true,
                enterAnimationDuration: 450,
                exitAnimationDuration: 250
            }
        },
        {
            provide: MAT_SNACK_BAR_DEFAULT_OPTIONS,
            useValue: {
                duration: 4500
            }
        }
    ],
    bootstrap: [AppComponent]
})
export class AppModule {
}
