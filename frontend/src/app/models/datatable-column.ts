import {PipeTransform, TemplateRef} from "@angular/core";
import {DatatableColumn as WizardryDatatableColumn} from "@biit-solutions/wizardry-theme/table";

/**
 * Datatable column with an explicit default width.
 *
 * ngx-datatable v27 converts the `width` input with `numberAttribute`, so an
 * `undefined` value becomes `NaN` and breaks the CSS grid used to lay out the
 * header and the rows. The wizardry-theme datatable forwards the column width
 * as-is, therefore every column created by the application must define one.
 * The default value matches the ngx-datatable default column width (150).
 */
export class DatatableColumn extends WizardryDatatableColumn {
  constructor(name: string, prop: string | number, visible?: boolean, width?: number, sortable?: boolean,
              pipe?: PipeTransform, cellTemplate?: TemplateRef<any>,
              cellClass?: string | ((data: unknown) => string | unknown), comparator?: Function) {
    super(name, prop, visible, width, sortable, pipe, cellTemplate, cellClass, comparator);
    this.width ??= 150;
  }
}
