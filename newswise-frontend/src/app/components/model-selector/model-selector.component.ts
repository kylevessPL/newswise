import {Component, EventEmitter, Output} from '@angular/core';
import {ModelEnum} from '../../model/model.enum';

@Component({
    selector: 'app-model-selector',
    templateUrl: './model-selector.component.html'
})
export class ModelSelectorComponent {
    @Output() modelEvent = new EventEmitter<ModelEnum>();

    protected readonly model = ModelEnum;
    protected modelValues: ModelEnum[] = Object.values(this.model);

    protected modelSelected = (model: ModelEnum) => this.modelEvent.emit(model);
}
