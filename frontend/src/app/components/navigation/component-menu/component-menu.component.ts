import {Component, OnInit, TemplateRef} from '@angular/core';
import {TemplateService} from "../../../services/template.service";

@Component({
  selector: 'component-menu',
  templateUrl: './component-menu.component.html',
  styleUrls: ['./component-menu.component.scss']
})
export class ComponentMenuComponent implements OnInit{
  template: TemplateRef<any> | null = null;

  constructor(private templateService: TemplateService) { }

  ngOnInit() {
    this.templateService.currentTemplate.subscribe(template => this.template = template);
  }
}
