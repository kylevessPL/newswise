import {Component, Inject} from '@angular/core';
import {MAT_DIALOG_DATA} from '@angular/material/dialog';
import {DocumentCategoryEnum} from '../../model/document-category.enum';
import {GeneralMetadataEnum} from '../../model/general-metadata.enum';
import {ImageMetadataEnum} from '../../model/image-metadata.enum';
import {TextMetadataEnum} from '../../model/text-metadata.enum';
import {DocumentProcessingSuccess} from '../../model/document-processing-success';
import {ImagePropertyEnum} from '../../model/image-property.enum';
import EnumUtil from '../../utils/enum.util';

interface Details {
    title: string;
    description: string;
    icon: string;
    data: Map<string, string>;
}

@Component({
    selector: 'app-processing-details',
    templateUrl: './processing-details-dialog.component.html',
    styleUrl: './processing-details-dialog.component.scss'

})
export class ProcessingDetailsDialogComponent {
    private readonly generalMetadataKeys = Object.keys(GeneralMetadataEnum);
    private readonly textMetadataKeys = Object.keys(TextMetadataEnum);
    private readonly imageMetadataKeys = Object.keys(ImageMetadataEnum);

    protected details: Details[];

    private step = 0;

    constructor(@Inject(MAT_DIALOG_DATA) document: DocumentProcessingSuccess) {
        this.details = this.createDetails(document);
    }

    protected open = (index: number) => {
        this.step = index;
    };

    protected isOpened = (index: number) => this.step === index;

    protected isFirst = () => this.step === 0;

    protected isLast = () => this.step === this.details.length - 1;

    protected previous = () => this.step === 0 ? this.step : this.step--;

    protected next = () => this.step === this.details.length - 1 ? this.step : this.step++;

    private createDetails = (document: DocumentProcessingSuccess) => {
        const predictions = this.processPredictions(document.predictions);
        const metadata = this.processMetadata(document.metadata);
        return [predictions, ...metadata];
    };

    private processPredictions = (predictions: Map<string, number>) => {
        const data = new Map(Array.from(predictions).map(([key, value]) =>
            [EnumUtil.getEnumValue(DocumentCategoryEnum, key)!!, `${value}%`]
        ));
        return {
            title: 'topic-predicitons',
            description: 'prediction-distribution-for-all-press-article-topics',
            icon: 'psychology_alt',
            data
        } as Details;
    };

    private processMetadata = (metadata: Map<string, any>) => {
        const generalProperties = new Map<string, string>();
        const textProperties = new Map<string, string>();
        const imageProperties = new Map<string, string>();
        Array.from(metadata)
            .map(([key, value]) => [key, value.toString()] as [string, string])
            .forEach(([key, value]) => {
                if (this.generalMetadataKeys.includes(key)) {
                    generalProperties.set(EnumUtil.getEnumValue(GeneralMetadataEnum, key)!!, value);
                } else if (this.textMetadataKeys.includes(key)) {
                    textProperties.set(EnumUtil.getEnumValue(TextMetadataEnum, key)!!, value);
                } else if (this.imageMetadataKeys.includes(key)) {
                    const property = EnumUtil.getEnumValue(ImagePropertyEnum, value) ?? value;
                    imageProperties.set(key, property);
                }
            });
        const generalMetadata: Details = {
            title: 'general-metadata',
            description: 'general-document-information',
            icon: 'info',
            data: generalProperties
        };
        const textMetadata: Details = {
            title: 'text-metadata',
            description: 'text-document-information',
            icon: 'article',
            data: textProperties
        };
        const imageMetadata: Details = {
            title: 'image-metadata',
            description: 'image-document-information',
            icon: 'image',
            data: imageProperties
        };
        return [generalMetadata, textMetadata, imageMetadata];
    };
}
