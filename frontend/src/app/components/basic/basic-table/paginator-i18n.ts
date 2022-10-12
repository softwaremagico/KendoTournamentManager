import {TranslateService} from "@ngx-translate/core";
import {MatPaginatorIntl} from "@angular/material/paginator";
import {Injectable} from "@angular/core";

@Injectable()
export class PaginatorI18n extends MatPaginatorIntl {
  translate: TranslateService;

  override getRangeLabel = (page: number, pageSize: number, length: number) => {
    const of = this.translate ? this.translate.instant('paginatorOf') : 'of';
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

  injectTranslateService(translate: TranslateService) {
    this.translate = translate;

    this.translate.onLangChange.subscribe(() => {
      this.translateLabels();
    });

    this.translateLabels();
  }

  translateLabels() {
    super.itemsPerPageLabel = this.translate.instant('paginatorItemsPerPage') !== 'paginatorItemsPerPage' ?
      this.translate.instant('paginatorItemsPerPage') : 'Items per page:';
    super.nextPageLabel = this.translate.instant('paginatorNextPage') !== 'paginatorNextPage' ?
      this.translate.instant('paginatorNextPage') : 'Next page';
    super.previousPageLabel = this.translate.instant('paginatorPreviousPage')!== 'paginatorPreviousPage' ?
      this.translate.instant('paginatorPreviousPage') : 'Previous page';
    super.firstPageLabel = this.translate.instant('paginatorFirstPage')!== 'paginatorFirstPage' ?
      this.translate.instant('paginatorFirstPage') : 'First page';
    super.lastPageLabel = this.translate.instant('paginatorLastPage')!== 'paginatorLastPage' ?
      this.translate.instant('paginatorLastPage') : 'Last page';
    this.changes.next();
  }
}
