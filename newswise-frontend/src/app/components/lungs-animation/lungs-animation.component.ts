import {Component, Input, NgZone, OnChanges} from '@angular/core';
import {AnimationOptions} from 'ngx-lottie';
import {ANIMATIONS_DIR} from '../../commons/app.constants';
import {AnimationItem} from 'lottie-web';

@Component({
    selector: 'app-lungs-animation',
    templateUrl: './lungs-animation.component.html'
})
export class LungsAnimationComponent implements OnChanges {
    options: AnimationOptions = {
        path: `${ANIMATIONS_DIR}/lungs.json`,
        autoplay: false,
        rendererSettings: {
            viewBoxOnly: true,
            viewBoxSize: '0 30 120 60'
        }
    };

    @Input() playing?: boolean;

    private animation?: AnimationItem;

    constructor(private ngZone: NgZone) {
    }

    ngOnChanges() {
        this.handlePlayingChange();
    }

    animationCreated(animation: AnimationItem) {
        this.animation = animation;
    }

    private handlePlayingChange = () => {
        this.playing ? this.play() : this.stop();
    };

    private play = () => this.ngZone.runOutsideAngular(() => this.animation?.play());

    private stop = () => this.ngZone.runOutsideAngular(() => this.animation?.stop());
}
