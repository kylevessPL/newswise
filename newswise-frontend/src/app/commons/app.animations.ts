import {animate, AUTO_STYLE, style, transition, trigger} from '@angular/animations';

export const Animations = {
    displayState: trigger('displayState', [
        transition(':enter', [
            style({overflow: 'hidden', height: 0}),
            animate(
                '300ms ease-in',
                style({overflow: 'hidden', height: AUTO_STYLE})
            )
        ]),
        transition(':leave', [
            style({overflow: 'hidden', height: AUTO_STYLE}),
            animate(
                '300ms ease-out',
                style({overflow: 'hidden', height: 0})
            )
        ])
    ])
};
