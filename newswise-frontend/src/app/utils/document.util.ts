import {DocumentProcessingData} from '../model/document-processing-data';
import {DocumentProcessingSuccess} from '../model/document-processing-success';
import {DocumentProcessingFailure} from '../model/document-processing-failure';

export default class DocumentUtil {
    static isSuccess = (document: DocumentProcessingData): document is DocumentProcessingSuccess =>
        'predictions' in document;

    static isFailure = (document: DocumentProcessingData): document is DocumentProcessingFailure =>
        'errorMessage' in document;
}
