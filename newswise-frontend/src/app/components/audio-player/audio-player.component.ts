import {AfterViewInit, Component, ElementRef, EventEmitter, Input, Output, ViewChild} from '@angular/core';

@Component({
    selector: 'app-audio-player',
    templateUrl: './audio-player.component.html',
    styleUrls: ['./audio-player.component.scss']
})
export class AudioPlayerComponent implements AfterViewInit {
    @Input() audio?: Blob;
    @Output() errorEvent = new EventEmitter();

    @ViewChild('player') private player?: ElementRef<HTMLMediaElement>;

    ngAfterViewInit() {
        this.player!.nativeElement.onerror = () => this.handleError();
    }

    private handleError() {
        if (this.audio) {
            this.errorEvent.emit();
        }
    }
}
