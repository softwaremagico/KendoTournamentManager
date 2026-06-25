import {Component, EventEmitter, Output} from '@angular/core';
import {TranslocoService} from "@ngneat/transloco";
import {UserSessionService} from "../../services/user-session.service";

@Component({
  standalone: false,
  selector: 'language-selector',
  templateUrl: './language-selector.component.html',
  styleUrls: ['./language-selector.component.scss']
})
export class LanguageSelectorComponent {

  protected languages: string[];
  protected selectedLanguage: string;

  @Output() closed: EventEmitter<void> = new EventEmitter<void>();

  constructor(private translocoService: TranslocoService, private userSessionService: UserSessionService) {
    this.languages = this.translocoService.getAvailableLangs() as string[];
    this.selectedLanguage = this.userSessionService.getLanguage();
  }

  close() {
    this.closed.emit();
  }

  switchLanguage(): void {
    this.translocoService.setActiveLang(this.selectedLanguage);
    this.userSessionService.setLanguage(this.selectedLanguage);
  }
}
