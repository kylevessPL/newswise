import {Pipe, PipeTransform} from '@angular/core';

@Pipe({name: 'resourcePipe'})
export class ResourcePipe implements PipeTransform {
    transform(resource?: string | URL): string | undefined {
        return typeof resource === 'string' ? resource : resource?.hostname;
    }
}
