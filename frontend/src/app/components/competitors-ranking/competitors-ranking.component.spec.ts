import {of} from 'rxjs';
import {Club} from '../../models/club';
import {Participant} from '../../models/participant';
import {ScoreOfCompetitor} from '../../models/score-of-competitor';
import {Tournament} from '../../models/tournament';
import {RankingService} from '../../services/ranking.service';
import {RbacService} from '../../services/rbac/rbac.service';
import {CompetitorsRankingComponent} from './competitors-ranking.component';

describe('CompetitorsRankingComponent', () => {
  let component: CompetitorsRankingComponent;
  let rankingServiceSpy: jasmine.SpyObj<RankingService>;
  let rbacServiceSpy: jasmine.SpyObj<RbacService>;

  beforeEach(() => {
    rankingServiceSpy = jasmine.createSpyObj('RankingService', [
      'getCompetitorsScoreRankingByClub',
      'getCompetitorsScoreRankingByTournament',
      'getCompetitorsGlobalScoreRanking',
      'getCompetitorsScoreRankingByClubAsPdf',
      'getCompetitorsScoreRankingByTournamentAsPdf',
      'getCompetitorsGlobalScoreRankingAsPdf'
    ]);
    rbacServiceSpy = jasmine.createSpyObj('RbacService', ['isAllowed']);

    component = new CompetitorsRankingComponent(
      rankingServiceSpy,
      {} as any,
      rbacServiceSpy
    );
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should load ranking by club when club has id', () => {
    const scores = [{ sortingIndex: 0 }] as unknown as ScoreOfCompetitor[];
    component.club = { id: 7, name: 'Club A' } as Club;
    rankingServiceSpy.getCompetitorsScoreRankingByClub.and.returnValue(of(scores));

    component.getRanking();

    expect(rankingServiceSpy.getCompetitorsScoreRankingByClub).toHaveBeenCalledOnceWith(7);
    expect(component.competitorsScore).toBe(scores);
  });

  it('should load ranking by tournament when tournament has id and no club', () => {
    const scores = [{ sortingIndex: 1 }] as unknown as ScoreOfCompetitor[];
    component.club = undefined;
    component.tournament = { id: 9, name: 'Tournament' } as Tournament;
    rankingServiceSpy.getCompetitorsScoreRankingByTournament.and.returnValue(of(scores));

    component.getRanking();

    expect(rankingServiceSpy.getCompetitorsScoreRankingByTournament).toHaveBeenCalledOnceWith(9);
    expect(component.competitorsScore).toBe(scores);
  });

  it('should load global ranking and schedule scroll when no club or tournament', () => {
    const scores = [{ sortingIndex: 2 }] as unknown as ScoreOfCompetitor[];
    component.club = undefined;
    component.tournament = undefined;
    component.numberOfDays = 30;
    component.competitor = { id: 11 } as Participant;
    rankingServiceSpy.getCompetitorsGlobalScoreRanking.and.returnValue(of(scores));
    spyOn(document, 'getElementById').and.returnValue(null);
    spyOn(component, 'scrollToScore');
    jasmine.clock().install();

    component.getRanking();
    jasmine.clock().tick(500);

    expect(rankingServiceSpy.getCompetitorsGlobalScoreRanking).toHaveBeenCalledOnceWith(undefined, 30);
    expect(component.competitorsScore).toBe(scores);
    expect(component.scrollToScore).toHaveBeenCalled();
    jasmine.clock().uninstall();
  });

  it('should emit onClosed when closeDialog is called', () => {
    spyOn(component.onClosed, 'emit');

    component.closeDialog();

    expect(component.onClosed.emit).toHaveBeenCalledOnceWith();
  });

  it('should download pdf by club when club has id', () => {
    const blob = new Blob(['pdf'], { type: 'application/pdf' });
    const anchorMock = { download: '', href: '', click: jasmine.createSpy('click') } as unknown as HTMLAnchorElement;
    component.club = { id: 3, name: 'Club PDF' } as Club;
    rankingServiceSpy.getCompetitorsScoreRankingByClubAsPdf.and.returnValue(of(blob));
    spyOn(window.URL, 'createObjectURL').and.returnValue('blob:club');
    spyOn(document, 'createElement').and.returnValue(anchorMock as any);

    component.downloadPDF();

    expect(rankingServiceSpy.getCompetitorsScoreRankingByClubAsPdf).toHaveBeenCalledOnceWith(3);
    expect(anchorMock.href).toBe('blob:club');
    expect((anchorMock.click as jasmine.Spy)).toHaveBeenCalled();
  });

  it('should download pdf by tournament when tournament has id and no club', () => {
    const blob = new Blob(['pdf'], { type: 'application/pdf' });
    const anchorMock = { download: '', href: '', click: jasmine.createSpy('click') } as unknown as HTMLAnchorElement;
    component.club = undefined;
    component.tournament = { id: 4, name: 'Tournament PDF' } as Tournament;
    rankingServiceSpy.getCompetitorsScoreRankingByTournamentAsPdf.and.returnValue(of(blob));
    spyOn(window.URL, 'createObjectURL').and.returnValue('blob:tournament');
    spyOn(document, 'createElement').and.returnValue(anchorMock as any);

    component.downloadPDF();

    expect(rankingServiceSpy.getCompetitorsScoreRankingByTournamentAsPdf).toHaveBeenCalledOnceWith(4);
    expect(anchorMock.href).toBe('blob:tournament');
    expect((anchorMock.click as jasmine.Spy)).toHaveBeenCalled();
  });

  it('should download global pdf when no club and no tournament', () => {
    const blob = new Blob(['pdf'], { type: 'application/pdf' });
    const anchorMock = { download: '', href: '', click: jasmine.createSpy('click') } as unknown as HTMLAnchorElement;
    component.club = undefined;
    component.tournament = undefined;
    component.numberOfDays = 14;
    rankingServiceSpy.getCompetitorsGlobalScoreRankingAsPdf.and.returnValue(of(blob));
    spyOn(window.URL, 'createObjectURL').and.returnValue('blob:global');
    spyOn(document, 'createElement').and.returnValue(anchorMock as any);

    component.downloadPDF();

    expect(rankingServiceSpy.getCompetitorsGlobalScoreRankingAsPdf).toHaveBeenCalledOnceWith(undefined, 14);
    expect(anchorMock.href).toBe('blob:global');
    expect((anchorMock.click as jasmine.Spy)).toHaveBeenCalled();
  });

  it('should scroll row into view when row exists', () => {
    const row = {
      scrollIntoView: jasmine.createSpy('scrollIntoView')
    } as unknown as HTMLElement;

    component.scrollToScore(row);

    expect((row.scrollIntoView as jasmine.Spy)).toHaveBeenCalledOnceWith({ behavior: 'smooth' });
  });

  it('should call getRanking when daysChanged is called', () => {
    spyOn(component, 'getRanking');

    component.daysChanged();

    expect(component.getRanking).toHaveBeenCalled();
  });
});

