import {animate, state, style, transition, trigger} from '@angular/animations';

export const Animations = {
    displayState: trigger('displayState', [
        state('false', style({overflow: 'hidden', height: '0px'})),
        state('true', style({overflow: 'hidden', height: '*'})),
        transition('false => true', animate('300ms ease-in')),
        transition('true => false', animate('300ms ease-out'))
    ])
};
