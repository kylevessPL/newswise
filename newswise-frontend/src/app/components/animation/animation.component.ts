import {Component, Input, NgZone} from '@angular/core';
import {AnimationOptions} from 'ngx-lottie';
import {AnimationItem} from 'lottie-web';

export type ViewBox = [number, number, number, number];

@Component({
    selector: 'app-animation',
    templateUrl: './animation.component.html'
})
export class AnimationComponent {
    @Input() path: string;
    @Input() viewBox: ViewBox;

    private animation?: AnimationItem;

    constructor(private ngZone: NgZone) {
    }

    protected options = () => ({
        path: this.path,
        rendererSettings: {
            viewBoxOnly: true,
            viewBoxSize: this.viewBox?.join(' ')
        }
    } as AnimationOptions);

    protected animationCreated = (animation: AnimationItem) => {
        this.animation = animation;
    };
}
