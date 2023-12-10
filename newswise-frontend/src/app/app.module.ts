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
import {PluckPipe} from './pipes/pluck.pipe';
import {AudioPlayerComponent} from './components/audio-player/audio-player.component';
import {BlobUrlPipe} from './pipes/blob-url.pipe';
import {AudioRecordComponent} from './components/audio-record/audio-record.component';
import {AudioContextModule} from 'angular-audio-context';
import {LottieCacheModule, LottieModule} from 'ngx-lottie';
import {LungsAnimationComponent} from './components/lungs-animation/lungs-animation.component';
import {MAT_DIALOG_DEFAULT_OPTIONS, MatDialogModule} from '@angular/material/dialog';
import {AnalysisResultDialogComponent} from './components/analysis-result-dialog/analysis-result-dialog.component';
import {NgOptimizedImage} from '@angular/common';
import {HTTP_INTERCEPTORS, HttpClient, HttpClientModule} from '@angular/common/http';
import {HttpErrorInterceptor} from './config/interceptors/http-error.interceptor';
import player from 'lottie-web/build/player/lottie_svg';
import {ObsTransformPipe} from './pipes/obs-transform.pipe';
import {ObsSkipFirstPipe} from './pipes/obs-skip-first.pipe';
import {MissingTranslationHandler, TranslateLoader, TranslateModule} from '@ngx-translate/core';
import {TranslateHttpLoader} from '@ngx-translate/http-loader';
import {FooterComponent} from './components/footer/footer.component';
import {MAT_FORM_FIELD_DEFAULT_OPTIONS, MatFormFieldModule} from '@angular/material/form-field';
import {MatSelectModule} from '@angular/material/select';
import {ReactiveFormsModule} from '@angular/forms';
import {LocalizationService} from './services/localization.service';
import {IconsService} from './services/icons.service';
import {InViewportModule} from 'ng-in-viewport';
import {AnalysisExportDialogComponent} from './components/analysis-export-dialog/analysis-export-dialog.component';
import {GlobalMissingTranslationHandler} from './config/handlers/global-missing-translation.handler';
import {SurveyModule} from 'survey-angular-ui';
import {PreliminaryStudyDialogComponent} from './components/preliminary-study-dialog/preliminary-study-dialog.component';
import {ConfirmationDialogComponent} from './components/confirmation-dialog/confirmation-dialog.component';

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
        PluckPipe,
        ObsSkipFirstPipe,
        ObsTransformPipe,
        BlobUrlPipe,
        AppComponent,
        FooterComponent,
        HeaderComponent,
        TextDividerComponent,
        FileUploadComponent,
        AudioRecordComponent,
        AudioPlayerComponent,
        LungsAnimationComponent,
        ConfirmationDialogComponent,
        PreliminaryStudyDialogComponent,
        AnalysisResultDialogComponent,
        AnalysisExportDialogComponent
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
        SurveyModule,
        NgOptimizedImage,
        InViewportModule,
        AudioContextModule.forRoot('balanced'),
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
        LottieModule.forRoot({
            player: () => player,
        }),
        LottieCacheModule.forRoot(),
        MatFormFieldModule,
        MatSelectModule,
        ReactiveFormsModule
    ],
    providers: [
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
                exitAnimationDuration: 250,
                maxWidth: '50vw'
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
