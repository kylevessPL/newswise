import {DocumentProcessingData} from './document-processing-data';

export interface DocumentProcessingFailure extends DocumentProcessingData {
    errorMessage: string;
}
