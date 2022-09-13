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
    super.itemsPerPageLabel = this.translate.instant('paginatorItemsPerPage');
    super.nextPageLabel = this.translate.instant('paginatorNextPage');
    super.previousPageLabel = this.translate.instant('paginatorPreviousPage');
    super.firstPageLabel = this.translate.instant('paginatorFirstPage');
    super.lastPageLabel = this.translate.instant('paginatorLastPage');
    this.changes.next();
  }
}
