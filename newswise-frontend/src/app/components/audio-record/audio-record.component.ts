import {Component, EventEmitter, Input, OnDestroy, OnInit, Output} from '@angular/core';
import {AudioRecordingService} from '../../services/audio-recording.service';
import {AudioRecordError} from '../../model/audio-record.error';
import {Observable, Subscription} from 'rxjs';

@Component({
    selector: 'app-audio-record',
    templateUrl: './audio-record.component.html'
})
export class AudioRecordComponent implements OnInit, OnDestroy {
    @Input() minDuration?: number;
    @Input() maxDuration?: number;
    @Input() externalChange?: Observable<boolean>;
    @Output() outputEvent = new EventEmitter<Blob>();
    @Output() errorEvent = new EventEmitter();

    recording = false;
    stopDisabled = false;

    private externalChangeSubscription?: Subscription;

    constructor(private audioRecorderService: AudioRecordingService) {
        this.audioRecorderService.getDuration().subscribe(value => this.processDuration(value));
        this.audioRecorderService.getOutput().subscribe(output => this.processOutput(output));
        this.audioRecorderService.getError().subscribe(error => this.processError(error));
    }

    ngOnInit() {
        this.externalChangeSubscription = this.externalChange?.subscribe(changed => this.handleExternalChange(changed));
    }

    ngOnDestroy = () => this.externalChangeSubscription?.unsubscribe();

    startRecording() {
        this.stopDisabled = !!this.minDuration;
        this.recording = true;
        this.audioRecorderService.startRecording();
    };

    stopRecording() {
        this.audioRecorderService.stopRecording();
        this.recording = false;
    }

    private handleExternalChange(changed: boolean) {
        if (changed) {
            this.audioRecorderService.cancelRecording();
            this.recording = false;
        }
    }

    private processDuration(duration: number) {
        this.stopDisabled = !!this.minDuration && this.minDuration > duration;
        if (this.maxDuration && this.maxDuration <= duration) {
            this.stopRecording();
        }
    }

    private processOutput = (blob: Blob) => this.outputEvent.emit(blob);

    private processError(error: AudioRecordError) {
        this.recording = false;
        this.errorEvent.emit(error);
        this.outputEvent.emit(undefined);
    }
}
