import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA} from '@angular/material/dialog';
import {ConfirmationData} from '../../model/confirmation.data';

@Component({
    selector: 'app-confirmation',
    templateUrl: './confirmation-dialog.component.html'
})
export class ConfirmationDialogComponent {
    constructor(@Inject(MAT_DIALOG_DATA) public data: ConfirmationData) {
    }
}
