import {Component, EventEmitter, OnDestroy, OnInit, Output} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import normalizeUrl from 'normalize-url';
import {map, Subscription} from 'rxjs';
import ValidationUtil from '../../utils/validation.util';

@Component({
    selector: 'app-url-input',
    templateUrl: './url-input.component.html',
    styleUrl: './url-input.component.scss'
})
export class UrlInputComponent implements OnInit, OnDestroy {
    @Output() urlEvent = new EventEmitter<URL>();

    protected form: FormGroup;

    private changeSubscription?: Subscription;

    constructor(private fb: FormBuilder) {
    }

    ngOnInit() {
        this.form = this.fb.group({
            url: ['', [], [ValidationUtil.urlValidator]]
        }, {updateOn: 'submit'});
        this.changeSubscription = this.form.get('url')?.valueChanges
            .pipe(map((value: string) => this.normalizeUrl(value)))
            .subscribe(value => this.setValue(value));
    }

    ngOnDestroy() {
        this.changeSubscription?.unsubscribe();
    }

    protected apply = () => {
        const url: string = this.form.get('url')?.value;
        this.urlEvent.emit(new URL(url));
    };

    protected hasValue = () => (this.form.get('url')?.value ?? '') !== '';

    protected hasError = () => this.form.get('url')?.hasError('invalidUrl') ?? false;

    private setValue = (value: string) => this.form.get('url')?.setValue(value, {emitEvent: false});

    private normalizeUrl = (url: string) => normalizeUrl(url, {
        stripAuthentication: false,
        stripTextFragment: false,
        removeQueryParameters: false,
        sortQueryParameters: false
    });
}
