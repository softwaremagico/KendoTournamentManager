import { getBalancedMember } from './members';
import { Participant } from '../../models/participant';

describe('getBalancedMember', () => {
  const buildParticipant = (id: number): Participant => ({
    id,
    name: 'Name' + id,
    lastname: 'Lastname' + id,
    idCard: 'ID' + id
  } as Participant);

  it('should pick from the first sector when selectFromSector is 0', () => {
    const participants = [buildParticipant(1), buildParticipant(2), buildParticipant(3)];

    const selected = getBalancedMember(participants, 0, 3);

    expect(selected.id).toBe(1);
  });

  it('should pick from the last sector when selectFromSector is the last index', () => {
    const participants = [buildParticipant(1), buildParticipant(2), buildParticipant(3)];

    const selected = getBalancedMember(participants, 2, 3);

    expect(selected.id).toBe(3);
  });

  it('should pick from a middle sector when selectFromSector is in between', () => {
    const participants = [buildParticipant(1), buildParticipant(2), buildParticipant(3)];

    const selected = getBalancedMember(participants, 1, 3);

    expect(selected.id).toBe(2);
  });

  it('should ignore undefined entries before selecting', () => {
    const participants = [undefined, buildParticipant(2), undefined, buildParticipant(4)] as (Participant | undefined)[];

    const selected = getBalancedMember(participants, 0, 2);

    expect(selected.id).toBe(2);
  });
});
