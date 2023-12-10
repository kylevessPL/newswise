import {Injectable} from '@angular/core';
import {forkJoin, Subject} from 'rxjs';
import {AudioRecordError} from '../model/audio-record.error';
import {IBlobEvent, IMediaRecorder, MediaRecorder, register} from 'extendable-media-recorder';
import {connect} from 'extendable-media-recorder-wav-encoder';

@Injectable({providedIn: 'root'})
export class AudioRecordingService {
    private duration = new Subject<number>();
    private output = new Subject<Blob>();
    private error = new Subject<AudioRecordError>();
    private registered = false;
    private cancelled = false;
    private stream?: MediaStream;
    private recorder?: IMediaRecorder;
    private data?: Blob[];
    private interval?: ReturnType<typeof setTimeout>;

    getDuration = () => this.duration.asObservable();
    getOutput = () => this.output.asObservable();
    getError = () => this.error.asObservable();

    startRecording() {
        if (this.recorder) {
            return;
        }
        this.cancelled = false;
        forkJoin([this.registerEncoder(), this.createStream()]).subscribe({
            next: ([_, stream]: [void, MediaStream]) => {
                this.stream = stream;
                this.recorder = this.createRecorder(stream);
                this.recorder.start();
            },
            error: error => this.handleError(error)
        });
    }

    stopRecording = () => this.recorder?.stop();

    cancelRecording() {
        this.cancelled = true;
        this.recorder?.stop();
    }

    private registerEncoder = async () => {
        if (this.registered) {
            return Promise.resolve();
        } else {
            await register(await connect());
            this.registered = true;
        }
    };

    private createStream = () => navigator.mediaDevices.getUserMedia({
        audio: {
            noiseSuppression: true,
            echoCancellation: true,
            sampleRate: 44000,
            sampleSize: 16
        }
    });

    private createRecorder(stream: MediaStream) {
        const recorder = new MediaRecorder(stream, {mimeType: 'audio/wav'});
        recorder.onstart = () => this.startMedia();
        recorder.onstop = () => this.stopMedia();
        recorder.onerror = error => this.handleError(error);
        recorder.ondataavailable = event => this.appendData(event);
        return recorder;
    }

    private startMedia() {
        const startDate = new Date();
        this.duration.next(0);
        this.interval = setInterval(() => {
            const diffTime = (new Date().getTime() - startDate.getTime()) / 1000;
            this.duration.next(diffTime);
        }, 500);
    }

    private stopMedia() {
        clearInterval(this.interval);
        !this.cancelled && this.createOutput();
        this.clearStream();
    }

    private handleError(event?: Event) {
        if ((event as any)?.error instanceof DOMException) {
            const type = (event as any).error.name === 'SecurityError'
                ? AudioRecordError.PERMISSIONS
                : AudioRecordError.GENERIC;
            this.error.next(type);
        } else {
            this.error.next(AudioRecordError.PERMISSIONS);
        }
    }

    private appendData(event: IBlobEvent) {
        this.data = this.data ?? [];
        if (event.data?.size > 0) {
            this.data.push(event.data);
        }
    }

    private createOutput() {
        const output = new Blob(this.data, {
            type: this.recorder?.mimeType
        });
        this.output.next(output);
    }

    private clearStream() {
        this.stream?.getAudioTracks().forEach(track => track.stop());
        this.data = undefined;
        this.recorder = undefined;
        this.stream = undefined;
    }
}
