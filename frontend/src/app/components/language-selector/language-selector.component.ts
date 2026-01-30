import {Component, EventEmitter, Output} from '@angular/core';
import {TranslocoService} from "@ngneat/transloco";
import {UserSessionService} from "../../services/user-session.service";

@Component({
  selector: 'language-selector',
  templateUrl: './language-selector.component.html',
  styleUrls: ['./language-selector.component.scss']
})
export class LanguageSelectorComponent {

  protected languages: string[];
  protected selectedLanguage: string;

  @Output() onClosed: EventEmitter<void> = new EventEmitter<void>();

  constructor(private translocoService: TranslocoService, private userSessionService: UserSessionService) {
    this.languages = this.translocoService.getAvailableLangs() as string[];
    this.selectedLanguage = this.userSessionService.getLanguage();
  }

  close() {
    this.onClosed.emit();
  }

  switchLanguage(): void {
    this.translocoService.setActiveLang(this.selectedLanguage);
    this.userSessionService.setLanguage(this.selectedLanguage);
  }
}
