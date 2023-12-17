import {Directive, ElementRef, HostBinding, Input, OnChanges} from '@angular/core';

@Directive({selector: '[smoothHeight]'})
export class SmoothHeightDirective implements OnChanges {
    @Input() smoothHeight: any;

    private pulse: boolean;
    private startHeight: number;

    @HostBinding('@grow')
    get grow() {
        return {value: this.pulse, params: {startHeight: this.startHeight}};
    }

    constructor(private element: ElementRef) {
    }

    ngOnChanges() {
        this.startHeight = this.element.nativeElement.clientHeight;
        this.pulse = !this.pulse;
    }
}
