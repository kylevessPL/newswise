import {animate, style, transition, trigger} from '@angular/animations';

export const Animations = {
    displayState: trigger('displayState', [
        transition(
            ':enter',
            [
                style({height: 0, opacity: 0}),
                animate('1s ease-out',
                    style({height: '*', opacity: 1}))
            ]
        ),
        transition(
            ':leave',
            [
                style({height: '*', opacity: 1}),
                animate('1s ease-in',
                    style({height: 0, opacity: 0}))
            ]
        )
    ])
};
