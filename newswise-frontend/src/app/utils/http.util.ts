import {HttpHeaders} from '@angular/common/http';
import {Headers} from 'http-constants-ts';
import {parse} from '@tinyhttp/content-disposition';

export default class HttpUtil {
    static downloadFile = (content: ArrayBuffer, headers: HttpHeaders) => {
        const contentDisposition = headers.get(Headers.CONTENT_DISPOSITION)!;
        const contentType = headers.get(Headers.CONTENT_TYPE)!;
        const file = new Blob([content], {type: contentType});
        const a = document.createElement('a');
        a.href = URL.createObjectURL(file);
        a.download = <string> parse(contentDisposition).parameters['filename'];
        a.click();
    };
}
