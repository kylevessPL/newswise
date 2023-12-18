import {Component, EventEmitter, Input, OnChanges, OnDestroy, OnInit, Output} from '@angular/core';
import {FormBuilder, FormGroup} from '@angular/forms';
import normalizeUrl from 'normalize-url';
import {Observable, Subscription} from 'rxjs';

@Component({
    selector: 'app-url-input',
    templateUrl: './url-input.component.html',
    styleUrl: './url-input.component.scss'
})
export class UrlInputComponent implements OnInit, OnChanges, OnDestroy {
    @Input() disabled = false;
    @Input() clearUrl: Observable<void>;
    @Output() urlEvent = new EventEmitter<URL>();

    protected form: FormGroup;

    private clearSubscription: Subscription;
    private changeSubscription?: Subscription;

    private get url() {
        return this.form?.get('url');
    }

    constructor(private fb: FormBuilder) {
    }

    ngOnInit() {
        this.form = this.fb.group({
            url: '',
        }, {updateOn: 'submit'});
        this.clearSubscription = this.clearUrl.subscribe(() => this.form.reset());
        this.changeSubscription = this.url?.valueChanges.subscribe(value => this.setValue(value));
    }

    ngOnChanges() {
        this.disabled ? this.url?.disable({emitEvent: false}) : this.url?.enable({emitEvent: false});
    }

    ngOnDestroy() {
        this.clearSubscription.unsubscribe();
        this.changeSubscription?.unsubscribe();
    }

    protected apply = () => {
        const url: string = this.url?.value;
        url !== '' && this.urlEvent.emit(new URL(url));
    };

    private setValue = (value: string) => {
        let url;
        try {
            url = this.normalizeUrl(value);
        } catch {
            url = '';
        }
        this.url?.setValue(url, {emitEvent: false});
    };

    private normalizeUrl = (url: string) => normalizeUrl(url, {
        stripAuthentication: false,
        stripTextFragment: false,
        removeQueryParameters: false,
        sortQueryParameters: false
    });
    protected readonly console = console;
}
