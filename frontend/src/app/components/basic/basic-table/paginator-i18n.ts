import {TranslocoService} from "@ngneat/transloco";
import {MatPaginatorIntl} from "@angular/material/paginator";
import {Injectable} from "@angular/core";

@Injectable()
export class PaginatorI18n extends MatPaginatorIntl {
  translate: TranslocoService;

  override getRangeLabel = (page: number, pageSize: number, length: number) => {
    const of = this.translate ? this.translate.translate('paginatorOf') : 'of';
    if (length === 0 || pageSize === 0) {
      return '0 ' + of + ' ' + length;
    }
    length = Math.max(length, 0);
    const startIndex = page * pageSize;
    // If the start index exceeds the list length, do not try and fix the end index to the end.
    const endIndex = startIndex < length ?
      Math.min(startIndex + pageSize, length) :
      startIndex + pageSize;
    return startIndex + 1 + ' - ' + endIndex + ' ' + of + ' ' + length;
  };

  injectTranslateService(translate: TranslocoService) {
    this.translate = translate;

    this.translate.langChanges$.subscribe(() => {
      this.translateLabels();
    });

    this.translateLabels();
  }

  translateLabels() {
    super.itemsPerPageLabel = this.translate.translate('paginatorItemsPerPage') !== 'paginatorItemsPerPage' ?
      this.translate.translate('paginatorItemsPerPage') : 'Items per page:';
    super.nextPageLabel = this.translate.translate('paginatorNextPage') !== 'paginatorNextPage' ?
      this.translate.translate('paginatorNextPage') : 'Next page';
    super.previousPageLabel = this.translate.translate('paginatorPreviousPage') !== 'paginatorPreviousPage' ?
      this.translate.translate('paginatorPreviousPage') : 'Previous page';
    super.firstPageLabel = this.translate.translate('paginatorFirstPage') !== 'paginatorFirstPage' ?
      this.translate.translate('paginatorFirstPage') : 'First page';
    super.lastPageLabel = this.translate.translate('paginatorLastPage') !== 'paginatorLastPage' ?
      this.translate.translate('paginatorLastPage') : 'Last page';
    this.changes.next();
  }
}
