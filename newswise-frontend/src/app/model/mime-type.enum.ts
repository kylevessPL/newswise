export class MimeType {
    static readonly AUDIO_WAV = new MimeType('audio/wav', '*.wav');
    static readonly AUDIO_MPEG = new MimeType('audio/mpeg', '*.mp3');
    static readonly AUDIO_OGG = new MimeType('audio/ogg', '*.ogg');

    private constructor(public readonly contentType: string, public readonly extension: string) {
    }
}
