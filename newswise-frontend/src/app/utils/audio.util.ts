import {AudioContext} from 'angular-audio-context';

export default class AudioUtil {
    static async blobToAudioBuffer(blob: Blob) {
        const audioContext = new AudioContext();
        const arrayBuffer = await blob.arrayBuffer();
        return await audioContext.decodeAudioData(arrayBuffer);
    }
}
