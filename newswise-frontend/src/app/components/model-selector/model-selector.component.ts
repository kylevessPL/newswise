import {Component, EventEmitter, Input, Output} from '@angular/core';
import {ModelEnum} from '../../model/model.enum';

@Component({
    selector: 'app-model-selector',
    templateUrl: './model-selector.component.html',
    styleUrl: './model-selector.component.scss'
})
export class ModelSelectorComponent {
    @Input() disabled = false;
    @Output() modelEvent = new EventEmitter<ModelEnum>();

    protected readonly model = ModelEnum;
    protected modelValues: ModelEnum[] = Object.values(this.model);

    protected modelSelected = (model: ModelEnum) => this.modelEvent.emit(model);
}
