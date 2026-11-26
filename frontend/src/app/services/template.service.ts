import {Injectable, TemplateRef} from '@angular/core';
import {BehaviorSubject} from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class TemplateService {
  private templateSource = new BehaviorSubject<TemplateRef<any> | null>(null);
  currentTemplate = this.templateSource.asObservable();

  constructor() { }

  changeTemplate(template: TemplateRef<any>) {
    this.templateSource.next(template);
  }
}
