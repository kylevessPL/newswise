import {Component, Input} from '@angular/core';
import {AnimationOptions} from 'ngx-lottie';

export type ViewBox = [number, number, number, number];

@Component({
    selector: 'app-animation',
    templateUrl: './animation.component.html'
})
export class AnimationComponent {
    @Input() path: string;
    @Input() viewBox: ViewBox;

    protected options = () => ({
        path: this.path,
        rendererSettings: {
            viewBoxOnly: true,
            viewBoxSize: this.viewBox?.join(' ')
        }
    } as AnimationOptions);
}
