import {DocumentProcessingData} from './document-processing-data';

export interface DocumentProcessingSuccess extends DocumentProcessingData {
    metadata: Map<string, any>;
    predictions: Map<string, number>;
}
