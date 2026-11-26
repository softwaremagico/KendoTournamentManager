import {DatePipe} from '@angular/common';
import {TranslocoService} from '@jsverse/transloco';
import {Club} from '../../models/club';
import {Participant} from '../../models/participant';
import {NameUtilsService} from '../../services/name-utils.service';
import {UserSessionService} from '../../services/user-session.service';
import {ClubNamePipe} from './club-name-pipe';
import {CustomDatePipe} from './custom-date-pipe';
import {ParticipantNamePipe} from './participant-name-pipe';
import {TableColumnTranslationPipe} from './table-column-translation-pipe';

describe('Visualization pipes', () => {
  it('renders the club name or an empty value', () => {
    const pipe = new ClubNamePipe();
    expect(pipe.transform({name: 'Kendo Club'} as Club)).toBe('Kendo Club');
    expect(pipe.transform(undefined)).toBe('');
  });

  it('delegates participant names and handles a missing participant', () => {
    const names = jasmine.createSpyObj<NameUtilsService>('NameUtilsService', ['getDisplayName']);
    const participant = {name: 'Aki'} as Participant;
    names.getDisplayName.and.returnValue('Aki Sato');
    const pipe = new ParticipantNamePipe(names);
    expect(pipe.transform(participant)).toBe('Aki Sato');
    expect(names.getDisplayName).toHaveBeenCalledWith(participant);
    expect(pipe.transform(undefined)).toBe('');
  });

  it('formats dates and normalizes missing dates to the epoch', () => {
    const datePipe = jasmine.createSpyObj<DatePipe>('DatePipe', ['transform']);
    datePipe.transform.and.returnValue('formatted');
    const pipe = new CustomDatePipe(datePipe);
    expect(pipe.transform(undefined)).toBe('formatted');
    expect(datePipe.transform).toHaveBeenCalledWith(0, 'dd/MM/yyyy');
    pipe.transform('2026-01-02');
    expect(datePipe.transform).toHaveBeenCalledWith('2026-01-02', 'dd/MM/yyyy');
  });

  it('translates booleans and enums while leaving numbers and empty cells unchanged', () => {
    const translate = jasmine.createSpyObj<TranslocoService>('TranslocoService', ['translate']);
    (translate.translate.and as any).callFake((key: string) => `translated:${key}`);
    const session = jasmine.createSpyObj<UserSessionService>('UserSessionService', ['getLanguage']);
    session.getLanguage.and.returnValue('en');
    const pipe = new TableColumnTranslationPipe(translate, session);
    expect(pipe.transform(7)).toBe(7);
    expect(pipe.transform(true)).toBe('translated:yes');
    expect(pipe.transform(false)).toBe('translated:no');
    expect(pipe.transform('TEAM_WINNER')).toBe('translated:teamWinner');
    expect(pipe.transform('hello')).toBe('translated:hello');
    expect(pipe.transform(null)).toBe('');
    expect(pipe.transform(undefined)).toBe('');
    expect(pipe.transform('')).toBe('');
  });

  it('translates object columns through their stable scalar key only', () => {
    const translate = jasmine.createSpyObj<TranslocoService>('TranslocoService', ['translate']);
    (translate.translate.and as any).callFake((key: string) => `translated:${key}`);
    const session = jasmine.createSpyObj<UserSessionService>('UserSessionService', ['getLanguage']);
    session.getLanguage.and.returnValue('en');
    const pipe = new TableColumnTranslationPipe(translate, session);
    expect(pipe.transform({scoreType: 'WIN_OVER_DRAWS'})).toBe('translated:winOverDraws');
    expect(pipe.transform({scoreType: 7})).toBe('');
    expect(pipe.transform({})).toBe('');
    expect(translate.translate).toHaveBeenCalledOnceWith('winOverDraws');
  });

  it('formats dates in translated table columns', () => {
    const translate = jasmine.createSpyObj<TranslocoService>('TranslocoService', ['translate']);
    const session = jasmine.createSpyObj<UserSessionService>('UserSessionService', ['getLanguage']);
    session.getLanguage.and.returnValue('en');
    const pipe = new TableColumnTranslationPipe(translate, session);
    const date = new Date(2026, 0, 2, 12, 0);
    expect(pipe.transform(date)).toBe(pipe.pipe.transform(date, 'short'));
    expect(pipe.transform('2026-01-02')).toBe(pipe.pipe.transform(new Date('2026-01-02'), 'short'));
  });
});
