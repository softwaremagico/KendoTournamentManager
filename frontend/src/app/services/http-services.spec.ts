import {HttpClient, HttpErrorResponse, HttpResponse} from '@angular/common/http';
import {CookieService} from 'ngx-cookie-service';
import {of, throwError} from 'rxjs';
import {EnvironmentService} from '../environment.service';
import {Club} from '../models/club';
import {Tournament} from '../models/tournament';
import {Participant} from '../models/participant';
import {Duel} from '../models/duel';
import {Fight} from '../models/fight';
import {Group} from '../models/group';
import {Team} from '../models/team';
import {AchievementsService} from './achievements.service';
import {AchievementType} from '../models/achievement-type.model';

import {ClubService} from './club.service';
import {CsvService} from './csv-service';
import {DuelService} from './duel.service';
import {FightService} from './fight.service';
import {FileService} from './file.service';
import {GroupService} from './group.service';
import {GroupLinkService} from './group-link.service';
import {InfoService} from './info.service';
import {LoggerService} from './logger.service';
import {LoginService} from './login.service';
import {MessageService} from './message.service';
import {ParticipantService} from './participant.service';
import {QrService} from './qr.service';
import {RankingService} from './ranking.service';
import {RoleService} from './role.service';
import {StatisticsService} from './statistics.service';
import {TeamService} from './team.service';
import {TournamentService} from './tournament.service';
import {UserService} from './user.service';
import {SystemOverloadService} from './notifications/system-overload.service';
import {TenantService} from './tenant.service';
import {TournamentExtendedPropertiesService} from './tournament-extended-properties.service';

describe('HTTP service endpoints', () => {
  let http: jasmine.SpyObj<HttpClient>;
  let environment: jasmine.SpyObj<EnvironmentService>;
  let messages: jasmine.SpyObj<MessageService>;
  let logger: jasmine.SpyObj<LoggerService>;
  let busy: SystemOverloadService;
  let login: LoginService;

  beforeEach(() => {
    http = jasmine.createSpyObj<HttpClient>('HttpClient', ['get', 'post', 'put', 'patch', 'delete']);
    for (const method of [http.get, http.post, http.put, http.patch, http.delete]) {
      (method.and as any).returnValue(of([]));
    }
    environment = jasmine.createSpyObj<EnvironmentService>('EnvironmentService', ['getBackendUrl']);
    environment.getBackendUrl.and.returnValue('/backend');
    messages = jasmine.createSpyObj<MessageService>('MessageService', ['handleError', 'logOnlyError', 'errorMessage']);
    (messages.handleError.and as any).returnValue(() => of([]));
    (messages.logOnlyError.and as any).returnValue(() => of([]));
    logger = jasmine.createSpyObj<LoggerService>('LoggerService', ['info']);
    busy = {isBusy: jasmine.createSpyObj('isBusy', ['next'])} as unknown as SystemOverloadService;
    login = {} as LoginService;
  });

  it('requests application information from the backend', () => {
    const service = new InfoService(http, messages, environment, logger);
    service.getLatestVersion().subscribe();
    expect(http.get as jasmine.Spy).toHaveBeenCalledWith('/backend/info/latest-version', {responseType: 'text'});
    service.getAppConfig().subscribe();
    expect(http.get).toHaveBeenCalledWith('/backend/info/app-config');
  });

  it('uses tenant-specific URLs and request bodies', () => {
    const service = new TenantService(http, environment);
    service.getAll().subscribe();
    expect(http.get).toHaveBeenCalledWith('/backend/auth/tenants');
    service.getActiveNames().subscribe();
    expect(http.get).toHaveBeenCalledWith('/backend/auth/public/tenants');
    service.getAvailable().subscribe();
    expect(http.get).toHaveBeenCalledWith('/backend/auth/tenants/available');
    service.update({id: 7, name: 'Club', active: false}).subscribe();
    expect(http.patch).toHaveBeenCalledWith('/backend/auth/tenants/7', {name: 'Club', active: false});
    service.assignUsers(7, ['a']).subscribe();
    expect(http.post).toHaveBeenCalledWith('/backend/auth/tenants/7/users', {usernames: ['a']});
    service.unassignUsers(7, ['a']).subscribe();
    expect(http.delete).toHaveBeenCalledWith('/backend/auth/tenants/7/users', {body: {usernames: ['a']}});
    service.delete(7).subscribe();
    expect(http.delete).toHaveBeenCalledWith('/backend/auth/tenants/7');
  });

  it('requests group links and clears the busy state', () => {
    const service = new GroupLinkService(http, environment, messages, logger, login, busy);
    service.getFromTournament(12).subscribe();
    expect(http.get).toHaveBeenCalledWith('/backend/groups-links/tournament/12');
    expect(busy.isBusy.next).toHaveBeenCalledWith(false);
  });

  it('uses the club REST endpoints for reads and writes', () => {
    const service = new ClubService(http, environment, messages, logger, login, busy);
    const club = {id: 5, name: 'Kendo'} as Club;
    service.getAll().subscribe();
    service.get(5).subscribe();
    service.add(club).subscribe();
    service.update(club).subscribe();
    service.deleteById(5).subscribe();
    service.delete(club).subscribe();
    expect(http.get).toHaveBeenCalledWith('/backend/clubs');
    expect(http.get).toHaveBeenCalledWith('/backend/clubs/5');
    expect(http.post).toHaveBeenCalledWith('/backend/clubs', club);
    expect(http.put).toHaveBeenCalledWith('/backend/clubs', club);
    expect(http.delete).toHaveBeenCalledWith('/backend/clubs/5');
    expect(http.post).toHaveBeenCalledWith('/backend/clubs/delete', club);
  });

  it('clears the busy state and delegates a failed club request', () => {
    (http.get.and as any).returnValue(throwError(() => new Error('offline')));
    const service = new ClubService(http, environment, messages, logger, login, busy);
    service.getAll().subscribe();
    expect(busy.isBusy.next).toHaveBeenCalledWith(false);
    expect(messages.handleError).toHaveBeenCalledWith('gets all');
  });

  it('requests tournament properties for a specific tournament', () => {
    const service = new TournamentExtendedPropertiesService(http, environment, login, messages, busy, logger);
    const tournament = {id: 4, name: 'League'} as Tournament;
    service.getByTournament(tournament).subscribe();
    service.getLatest().subscribe();
    expect(http.get).toHaveBeenCalledWith('/backend/tournaments/properties/tournaments/4');
    expect(http.get).toHaveBeenCalledWith('/backend/tournaments/properties/latest');
    expect(busy.isBusy.next).toHaveBeenCalledWith(false);
  });

  it('requests and regenerates achievements for the right participant or tournament', () => {
    const service = new AchievementsService(http, environment, messages, logger, login, busy);
    service.getParticipantAchievements(2).subscribe();
    service.getTournamentAchievements(3).subscribe();
    service.regenerateTournamentAchievements(3).subscribe();
    service.countByTypes().subscribe();
    expect(http.get).toHaveBeenCalledWith('/backend/achievements/participants/2');
    expect(http.get).toHaveBeenCalledWith('/backend/achievements/tournaments/3');
    expect(http.patch).toHaveBeenCalledWith('/backend/achievements/tournaments/3', null);
    expect(http.get).toHaveBeenCalledWith('/backend/achievements/count/types');
  });

  it('uses the participant endpoints for reads and updates', () => {
    const service = new ParticipantService(http, environment, messages, logger, login, busy);
    const participant = {id: 8, name: 'Aki'} as Participant;
    service.getAll().subscribe();
    service.get(8).subscribe();
    service.getByUsername().subscribe();
    service.add(participant).subscribe();
    service.update(participant).subscribe();
    service.delete(participant).subscribe();
    expect(http.get).toHaveBeenCalledWith('/backend/participants');
    expect(http.get).toHaveBeenCalledWith('/backend/participants/8');
    expect(http.get).toHaveBeenCalledWith('/backend/participants/jwt');
    expect(http.post).toHaveBeenCalledWith('/backend/participants', participant);
    expect(http.put).toHaveBeenCalledWith('/backend/participants', participant);
    expect(http.post).toHaveBeenCalledWith('/backend/participants/delete', participant);
  });

  it('scopes duel requests to the selected group, tournament and participant', () => {
    const service = new DuelService(http, environment, messages, logger, login);
    const duel = new Duel();
    service.update(duel).subscribe();
    service.getUntiesFromGroup(1).subscribe();
    service.getUntiesFromTournament(2).subscribe();
    service.getUntiesFromParticipant(3).subscribe();
    service.getDuelsFromParticipant(4).subscribe();
    expect(http.put).toHaveBeenCalledWith('/backend/duels', duel);
    expect(http.get).toHaveBeenCalledWith('/backend/duels/groups/1/unties');
    expect(http.get).toHaveBeenCalledWith('/backend/duels/tournaments/2/unties');
    expect(http.get).toHaveBeenCalledWith('/backend/duels/participant/3/unties');
    expect(http.get).toHaveBeenCalledWith('/backend/duels/competitor/4');
  });

  it('includes the frontend port in QR requests only when provided', () => {
    const service = new QrService(http, environment, messages, logger, login, busy);
    const tournament = {id: 4, name: 'League'} as Tournament;
    service.getGuestsQr(tournament, true).subscribe();
    service.getGuestsQr(tournament, true, 4200).subscribe();
    service.getParticipantQr(7, false, 4200).subscribe();
    expect(http.get).toHaveBeenCalledWith('/backend/qr/guest/tournament/4');
    expect(http.get).toHaveBeenCalledWith('/backend/qr/guest/tournament/4/port/4200?nightMode=true');
    expect(http.get).toHaveBeenCalledWith('/backend/qr/participant/7/statistics/port/4200?nightMode=false');
  });

  it('addresses groups within the requested tournament', () => {
    const service = new GroupService(http, environment, messages, logger, login, busy);
    const group = {id: 3} as Group;
    service.getAll().subscribe();
    service.getFromTournament(7).subscribe();
    service.getFromTournamentByIndex(7, 2, 3).subscribe();
    service.update(group).subscribe();
    service.setTeamsToGroup(3, []).subscribe();
    service.addTeamsToGroup(3, []).subscribe();
    service.deleteTeamsFromGroup(3, []).subscribe();
    expect(http.get).toHaveBeenCalledWith('/backend/groups');
    expect(http.get).toHaveBeenCalledWith('/backend/groups/tournaments/7');
    expect(http.get).toHaveBeenCalledWith('/backend/groups/tournaments/7/level/2/index/3');
    expect(http.put).toHaveBeenCalledWith('/backend/groups', group);
    expect(http.put).toHaveBeenCalledWith('/backend/groups/3/teams', []);
    expect(http.patch).toHaveBeenCalledWith('/backend/groups/3/teams/add', []);
    expect(http.patch).toHaveBeenCalledWith('/backend/groups/3/teams/delete', []);
  });

  it('uses fight endpoints for tournament scoring and updates', () => {
    const service = new FightService(http, environment, messages, logger, login, busy);
    const tournament = {id: 7, name: 'League'} as Tournament;
    const fight = {id: 3} as Fight;
    service.getAll().subscribe();
    service.getFromTournament(tournament).subscribe();
    service.deleteById(3).subscribe();
    service.delete(fight).subscribe();
    service.add(fight).subscribe();
    service.update(fight).subscribe();
    service.updateAll([fight]).subscribe();
    expect(http.get).toHaveBeenCalledWith('/backend/fights');
    expect(http.get).toHaveBeenCalledWith('/backend/fights/tournaments/7');
    expect(http.delete).toHaveBeenCalledWith('/backend/fights/3');
    expect(http.post).toHaveBeenCalledWith('/backend/fights/delete', fight);
    expect(http.post).toHaveBeenCalledWith('/backend/fights', fight);
    expect(http.put).toHaveBeenCalledWith('/backend/fights', fight);
    expect(http.put).toHaveBeenCalledWith('/backend/fights/all', [fight]);
  });

  it('uses team endpoints for reads and writes', () => {
    const service = new TeamService(http, environment, messages, logger, login, busy);
    const tournament = {id: 7, name: 'League'} as Tournament;
    const team = {id: 3, name: 'Team'} as Team;
    service.getAll().subscribe();
    service.get(3).subscribe();
    service.getFromTournament(tournament).subscribe();
    service.getRemainingFromTournament(tournament).subscribe();
    service.deleteById(3).subscribe();
    service.delete(team).subscribe();
    service.update(team).subscribe();
    expect(http.get).toHaveBeenCalledWith('/backend/teams');
    expect(http.get).toHaveBeenCalledWith('/backend/teams/3');
    expect(http.get).toHaveBeenCalledWith('/backend/teams/tournaments/7');
    expect(http.get).toHaveBeenCalledWith('/backend/teams/remaining/tournaments/7');
    expect(http.delete).toHaveBeenCalledWith('/backend/teams/3');
    expect(http.post).toHaveBeenCalledWith('/backend/teams/delete', team);
    expect(http.put).toHaveBeenCalledWith('/backend/teams', team);
  });

  it('retrieves roles for the requested tournament', () => {
    const service = new RoleService(http, environment, messages, logger, busy, login);
    service.getAll().subscribe();
    service.get(3).subscribe();
    service.getFromTournament(7).subscribe();
    service.getFromTournamentAndType(7, 'REFEREE' as any).subscribe();
    service.getFromTournamentAndTypes(7, ['REFEREE', 'SHINPAN'] as any).subscribe();
    service.deleteById(3).subscribe();
    expect(http.get).toHaveBeenCalledWith('/backend/roles');
    expect(http.get).toHaveBeenCalledWith('/backend/roles/3');
    expect(http.get).toHaveBeenCalledWith('/backend/roles/tournaments/7');
    expect(http.get).toHaveBeenCalledWith('/backend/roles/tournaments/7/types/REFEREE');
    expect(http.get).toHaveBeenCalledWith('/backend/roles/tournaments/7/types/REFEREE,SHINPAN');
    expect(http.delete).toHaveBeenCalledWith('/backend/roles/3');
  });

  it('uploads CSV files to the right club, participant, team and group-link endpoints', () => {
    const service = new CsvService(http, environment, messages, logger, login, busy);
    const file = new File(['id,name'], 'participants.csv');

    service.addClubs(file).subscribe();
    service.addParticipants(file).subscribe();
    service.addTeams(file, undefined).subscribe();
    service.addTeams(file, 7).subscribe();
    service.addGroupLinks(file, 7).subscribe();

    expect(http.post).toHaveBeenCalledWith('/backend/csv/clubs', jasmine.any(FormData));
    expect(http.post).toHaveBeenCalledWith('/backend/csv/participants', jasmine.any(FormData));
    expect(http.post).toHaveBeenCalledWith('/backend/csv/teams', jasmine.any(FormData));
    expect(http.post).toHaveBeenCalledWith('/backend/csv/teams/tournaments/7', jasmine.any(FormData));
    expect(http.post).toHaveBeenCalledWith('/backend/csv/groups-link/tournaments/7', jasmine.any(FormData));
    expect(logger.info).toHaveBeenCalled();
  });

  it('reads and writes tournament, fight, group and file resources', () => {
    const tournaments = new TournamentService(http, environment, login, messages, busy, logger);
    const fights = new FightService(http, environment, messages, logger, login, busy);
    const groups = new GroupService(http, environment, messages, logger, login, busy);
    const files = new FileService(http, environment, messages, logger, login, busy);
    const tournament = {id: 7, name: 'League'} as Tournament;
    const fight = {id: 3} as Fight;
    const group = {id: 3} as Group;
    const participant = {id: 8, name: 'Aki'} as Participant;
    const file = new File(['image'], 'avatar.png');

    tournaments.getAll().subscribe();
    tournaments.get(7).subscribe();
    tournaments.clone(7).subscribe();
    fights.deleteCollection([fight]).subscribe();
    fights.create(7, 0).subscribe();
    fights.generateDuels(fight).subscribe();
    (http.put as jasmine.Spy).and.returnValue(of(new HttpResponse<Fight[]>({status: 204, body: null})));
    fights.createNext(7).subscribe(fightsResult => expect(fightsResult).toEqual([]));
    groups.deleteTeamsFromTournament(7, []).subscribe();
    groups.addGroup(group).subscribe();
    groups.deleteGroup(group).subscribe();
    tournaments.deleteById(7);
    tournaments.setNumberOfWinners(tournament, 1).subscribe();
    tournaments.getParticipantAccreditation(7, participant, undefined).subscribe();
    files.setParticipantFilePicture(file, participant).subscribe();
    files.getTournamentPicture(tournament, '' as any).subscribe();
    files.deleteParticipantPicture(participant).subscribe();

    expect(http.get).toHaveBeenCalledWith('/backend/tournaments');
    expect(http.get).toHaveBeenCalledWith('/backend/tournaments/7');
    expect(http.get).toHaveBeenCalledWith('/backend/tournaments/7/clone');
    expect(http.post).toHaveBeenCalledWith('/backend/fights/delete/list', [fight]);
    expect(http.put).toHaveBeenCalledWith('/backend/fights/create/tournaments/7/levels/0', undefined);
    expect(http.put).toHaveBeenCalledWith('/backend/fights/duels', fight);
    expect(http.patch).toHaveBeenCalledWith('/backend/groups/tournaments/7/teams/delete', []);
    expect(http.post).toHaveBeenCalledWith('/backend/groups', group);
    expect(http.get).toHaveBeenCalledWith('/backend/files/tournaments/7/type/');
    expect(http.delete).toHaveBeenCalledWith('/backend/files/participants/8');
  });

  it('queries rankings, statistics and user accounts', () => {
    const rankings = new RankingService(http, environment, messages, logger, login, busy);
    const statistics = new StatisticsService(http, environment, messages, logger, login, busy);
    const users = new UserService(http, environment, {} as CookieService, busy, login, logger, messages);

    rankings.getCompetitorsScoreRankingByGroup(1).subscribe();
    rankings.getCompetitorsScoreRankingByTournament(7).subscribe();
    rankings.getCompetitorsRanking(8).subscribe();
    rankings.getCompetitorsGlobalScoreRanking([], 7).subscribe();
    rankings.getCompetitorsGlobalScoreRanking([], undefined).subscribe();
    rankings.getTeamsScoreRankingByTournament(7).subscribe();
    statistics.getFightStatistics(7, true, false).subscribe();
    statistics.getTournamentStatistics(7).subscribe();
    statistics.getYourWorstNightmare(8).subscribe();
    users.getAll().subscribe();
    users.getRoles().subscribe();

    expect(http.get).toHaveBeenCalledWith('/backend/rankings/competitors/groups/1');
    expect(http.get).toHaveBeenCalledWith('/backend/rankings/competitors/8');
    expect(http.get).toHaveBeenCalledWith('/backend/rankings/teams/tournaments/7');
    expect(http.get).toHaveBeenCalledWith('/backend/statistics/tournaments/7/fights', jasmine.any(Object));
    expect(http.get).toHaveBeenCalledWith('/backend/statistics/tournaments/7');
    expect(http.get).toHaveBeenCalledWith('/backend/statistics/participants/your-worst-nightmare/8');
    expect(http.get).toHaveBeenCalledWith('/backend/auth/register');
    expect(http.get).toHaveBeenCalledWith('/backend/auth/roles');

    (http.post as jasmine.Spy).and.returnValue(throwError(() => new HttpErrorResponse({status: 400})));
    users.add({username: 'aki'} as any).subscribe();
    expect(messages.errorMessage).toHaveBeenCalledWith('errorUserAlreadyExists');
  });

  it('logs at every severity through the logger endpoint', () => {
    const service = new LoggerService(http, environment, login);
    service.info('info');
    service.warning('warning');
    service.error('error');
    expect(http.post).toHaveBeenCalledWith('/backend/logger/info', jasmine.objectContaining({message: 'info'}));
    expect(http.post).toHaveBeenCalledWith('/backend/logger/warning', jasmine.objectContaining({message: 'warning'}));
    expect(http.post).toHaveBeenCalledWith('/backend/logger/error', jasmine.objectContaining({message: 'error'}));

    spyOn(service, 'error').and.callThrough();
    service.handleError<string>('saving')(new Error('offline')).subscribe();
    expect(service.error).toHaveBeenCalledWith('saving failed: offline');
  });

  it('queries achievement counts and regeneration endpoints', () => {
    const service = new AchievementsService(http, environment, messages, logger, login, busy);

    service.getParticipantAchievements(8).subscribe();
    service.getTournamentAchievements(7).subscribe();
    service.regenerateTournamentAchievements(7).subscribe();
    service.countByTypes().subscribe();
    service.countByType(AchievementType.THE_WINNER).subscribe();

    expect(http.get).toHaveBeenCalledWith('/backend/achievements/participants/8');
    expect(http.get).toHaveBeenCalledWith('/backend/achievements/tournaments/7');
    expect(http.patch).toHaveBeenCalledWith('/backend/achievements/tournaments/7', null);
    expect(http.get).toHaveBeenCalledWith('/backend/achievements/count/types');
    expect(http.get).toHaveBeenCalledWith(`/backend/achievements/count/types/${AchievementType.THE_WINNER}`);
  });

  it('covers the remaining tournament, accreditation and ranking endpoints', () => {
    const tournaments = new TournamentService(http, environment, login, messages, busy, logger);
    const rankings = new RankingService(http, environment, messages, logger, login, busy);
    const tournament = {id: 7, name: 'League'} as Tournament;
    const participant = {id: 8, name: 'Aki'} as Participant;

    tournaments.delete(tournament).subscribe();
    tournaments.add(tournament).subscribe();
    tournaments.update(tournament).subscribe();
    tournaments.getAccreditations(7, true, []).subscribe();
    tournaments.getParticipantAccreditation(7, participant, undefined).subscribe();
    tournaments.getDiplomas(7, true, []).subscribe();
    tournaments.getParticipantDiploma(7, participant).subscribe();
    tournaments.getLastUnlockedTournament().subscribe();

    rankings.getCompetitorsScoreRankingByTournamentAsPdf(7).subscribe();
    rankings.getCompetitorsGlobalScoreRankingAsPdf([participant], 7).subscribe();
    rankings.getCompetitorsScoreRankingByClub(3).subscribe();
    rankings.getCompetitorsScoreRankingByClubAsPdf(3).subscribe();
    rankings.getTeamsScoreRankingByGroup(1).subscribe();
    rankings.getTeamsScoreRankingByTournamentAsPdf(7).subscribe();
    rankings.getTeamsScoreRankingByGroupAsPdf(1).subscribe();
    rankings.getTournamentSummaryAsHtml(7).subscribe();
    rankings.getAllListAsZip(7).subscribe();

    expect(http.get).toHaveBeenCalledWith('/backend/tournaments/unlocked/lasts');
    expect((http.get as jasmine.Spy).calls.allArgs().some(args => args[0] === '/backend/rankings/summary/7/html')).toBeTrue();
    expect((http.get as jasmine.Spy).calls.allArgs().some(args => args[0] === '/backend/rankings/tournament/7/zip')).toBeTrue();
  });

  it('covers remaining team, group and fight write endpoints', () => {
    const teams = new TeamService(http, environment, messages, logger, login, busy);
    const groups = new GroupService(http, environment, messages, logger, login, busy);
    const fights = new FightService(http, environment, messages, logger, login, busy);
    const tournament = {id: 7, name: 'League'} as Tournament;
    const team = {id: 3, name: 'Team'} as Team;
    const participant = {id: 8, name: 'Aki'} as Participant;
    const fight = {id: 3} as Fight;

    teams.deleteByMemberAndTournament(participant, tournament).subscribe();
    teams.deleteByMembersAndTournament([participant], tournament).subscribe();
    teams.deleteByTournament(tournament).subscribe();
    teams.createByTournament(tournament).subscribe();
    teams.add(team).subscribe();
    teams.setAll([team]).subscribe();
    teams.getTeamsByTournament(7).subscribe();

    groups.deleteAllTeamsFromTournament(7).subscribe();
    groups.setTeams([]).subscribe();
    groups.addUnties(3, []).subscribe();
    groups.getGroupsByTournament(7).subscribe();
    groups.refreshNonStartedGroups(7, 0).subscribe();

    fights.deleteByTournament(tournament).subscribe();
    fights.addCollection([fight]).subscribe();
    fights.getFightSummaryPDf(7).subscribe();
    fights.getFromParticipant(8).subscribe();

    expect((http.get as jasmine.Spy).calls.allArgs().some(args => args[0] === '/backend/teams/tournaments/7/pdf')).toBeTrue();
    expect((http.get as jasmine.Spy).calls.allArgs().some(args => args[0] === '/backend/groups/tournaments/7/pdf')).toBeTrue();
    expect((http.get as jasmine.Spy).calls.allArgs().some(args => args[0] === '/backend/fights/tournaments/7/pdf')).toBeTrue();
    expect(http.get).toHaveBeenCalledWith('/backend/fights/competitor/8');
  });

  it('covers remaining file, role, user, property and statistics endpoints', () => {
    const files = new FileService(http, environment, messages, logger, login, busy);
    const roles = new RoleService(http, environment, messages, logger, busy, login);
    const users = new UserService(http, environment, {} as CookieService, busy, login, logger, messages);
    const properties = new TournamentExtendedPropertiesService(http, environment, login, messages, busy, logger);
    const statistics = new StatisticsService(http, environment, messages, logger, login, busy);
    const tournament = {id: 7, name: 'League'} as Tournament;
    const participant = {id: 8, name: 'Aki'} as Participant;
    const file = new File(['image'], 'avatar.png');

    files.setTournamentFilePicture(file, tournament, 'logo' as any, 'NONE' as any).subscribe();
    files.setBase64Picture({} as any).subscribe();
    files.getParticipantPicture(participant).subscribe();
    files.deleteTournamentPicture(tournament, 'logo' as any).subscribe();

    roles.delete({id: 3} as any).subscribe();
    roles.deleteByParticipantAndTournament(participant, tournament).subscribe();
    roles.add({id: 3} as any).subscribe();
    roles.update({id: 3} as any).subscribe();
    roles.getRolesByTournament(7).subscribe();

    users.add({username: 'aki'} as any).subscribe();
    users.update({username: 'aki'} as any).subscribe();
    users.delete({username: 'aki'} as any).subscribe();
    users.updatePassword('old', 'new').subscribe();
    users.updateUserPassword('aki', 'new').subscribe();

    properties.getByTournamentAndKey(tournament, 'ACHIEVEMENTS' as any).subscribe();
    properties.add({} as any).subscribe();
    properties.update({} as any).subscribe();

    statistics.getPreviousTournamentStatistics(7, 3).subscribe();
    statistics.getParticipantStatistics(8).subscribe();
    statistics.getWorstNightmareOf(8).subscribe();

    expect(http.post).toHaveBeenCalledWith('/backend/files/participants', {} as any);
    expect((http.get as jasmine.Spy).calls.allArgs().some(args => args[0] === '/backend/roles/tournaments/7/pdf')).toBeTrue();
    expect(http.post).toHaveBeenCalledWith('/backend/auth/password', {oldPassword: 'old', newPassword: 'new'});
    expect(http.get).toHaveBeenCalledWith('/backend/statistics/tournaments/7/previous/3');
  });
});
