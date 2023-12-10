import {Component, Inject, OnInit} from '@angular/core';
import {AnalysisExportPeriodData} from '../../model/analysis-export-period.data';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {first, map, Observable} from 'rxjs';
import {FormBuilder, FormGroup, Validators} from '@angular/forms';

@Component({
    selector: 'app-analysis-export',
    templateUrl: './analysis-export-dialog.component.html',
    styleUrls: ['./analysis-export-dialog.component.scss']
})
export class AnalysisExportDialogComponent implements OnInit {
    form: FormGroup;

    constructor(@Inject(MAT_DIALOG_DATA) public periods: Observable<AnalysisExportPeriodData[]>,
                private dialogRef: MatDialogRef<AnalysisExportDialogComponent>,
                private formBuilder: FormBuilder) {
        this.form = this.formBuilder.group({
            period: [null, [Validators.required]]
        });
    }

    ngOnInit() {
        this.periods
            .pipe(first(), map(periods => periods[0] ?? null))
            .subscribe(period => this.form.setValue({period: period}));
    }
}
