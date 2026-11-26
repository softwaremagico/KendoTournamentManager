import {NameUtilsService} from './name-utils.service';

describe('NameUtilsService', () => {
  const service = new NameUtilsService();
  const participant = {
    name: 'aki',
    lastname: 'doe smith'
  } as any;

  it('returns empty values for a missing participant', () => {
    expect(service.getName(undefined)).toBe('');
    expect(service.getShortName(undefined)).toBe('');
    expect(service.getShortLastName(undefined)).toBe('');
    expect(service.getLastname(undefined)).toBe('');
    expect(service.getLastnameNameCamelCase(undefined)).toBe('');
    expect(service.getNameLastname(undefined)).toBe(' ');
    expect(service.getLastnameNameNoSpaces(undefined)).toBe(', ');
    expect(service.getAcronym(undefined)).toBe('');
    expect(service.getInitials(undefined)).toBe('');
    expect(service.getDisplayName(undefined)).toBe(', ');
  });

  it('formats names, surnames, initials and acronyms', () => {
    expect(service.getName(participant)).toBe('aki');
    expect(service.getShortName(participant)).toBe('A.');
    expect(service.getShortLastName(participant)).toBe('doe');
    expect(service.getLastname(participant)).toBe('Doe Smith');
    expect(service.getLastnameNameCamelCase(participant)).toBe('doeSmithA.');
    expect(service.getNameLastname(participant)).toBe('aki Doe Smith');
    expect(service.getLastnameNameNoSpaces(participant)).toBe('Doe Smith, A.');
    expect(service.getAcronym(participant)).toBe('d. a.');
    expect(service.getInitials(participant)).toBe('ad');
  });

  it('selects the display name by resolution', () => {
    expect(service.getDisplayName(participant)).toBe('Doe Smith, aki');
    expect(service.getDisplayName(participant, 1600)).toBe('Doe Smith, aki');
    expect(service.getDisplayName(participant, 1300)).toBe('Doe Smith, A.');
    expect(service.getDisplayName(participant, 1100)).toBe('doe, A.');
    expect(service.getDisplayName(participant, 950)).toBe('doe');
    expect(service.getDisplayName(participant, 800)).toBe('d. a.');
  });

  it('joins team members with newlines', () => {
    expect(service.getTeamMembers({members: [participant]} as any)).toBe('aki Doe Smith\n');
  });
});
