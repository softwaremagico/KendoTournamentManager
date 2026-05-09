import {Participant} from './participant';
import {ParticipantImage} from './participant-image.model';

describe('ParticipantImage', () => {
  const buildParticipant = (): Participant => {
    const participant = new Participant();
    participant.id = 7;
    participant.name = 'Alice';
    participant.lastname = 'Doe';
    participant.idCard = 'ABC123';
    return participant;
  };

  const buildImage = (): ParticipantImage => {
    const image = new ParticipantImage();
    image.id = 10;
    image.data = 'img';
    image.imageFormat = 'JPG' as any;
    image.participant = buildParticipant();
    return image;
  };

  it('should copy nested participant when present', () => {
    const source = buildImage();
    const target = new ParticipantImage();

    ParticipantImage.copy(source, target);

    expect(target.participant).toBeTruthy();
    expect(target.participant).not.toBe(source.participant);
    expect(target.participant.id).toBe(7);
    expect(target.data).toBe('img');
  });

  it('should clone participant image into a new instance', () => {
    const source = buildImage();

    const clone = ParticipantImage.clone(source);

    expect(clone).not.toBe(source);
    expect(clone.participant).not.toBe(source.participant);
    expect(clone.participant.name).toBe('Alice');
  });

  it('should not assign participant when source participant is undefined', () => {
    const source = new ParticipantImage();
    const target = new ParticipantImage();

    ParticipantImage.copy(source, target);

    expect(target.participant).toBeUndefined();
  });
});

