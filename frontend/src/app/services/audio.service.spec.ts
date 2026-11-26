import {AudioService} from './audio.service';

describe('AudioService', () => {
  let service: AudioService;
  let audio: jasmine.SpyObj<HTMLAudioElement>;

  beforeEach(() => {
    service = new AudioService();
    audio = {
      play: jasmine.createSpy('play'),
      pause: jasmine.createSpy('pause'),
      load: jasmine.createSpy('load'),
      muted: false,
      loop: false
    } as unknown as jasmine.SpyObj<HTMLAudioElement>;
    audio.play.and.returnValue(Promise.resolve());
  });

  it('plays and stops the alarm', () => {
    spyOn(service, 'getAlarm').and.returnValue(audio);
    service.playAlarm();
    expect(audio.play).toHaveBeenCalled();
    service.stopAlarm();
    expect(audio.pause).toHaveBeenCalled();
    expect(audio.load).toHaveBeenCalled();
  });

  it('plays and stops the whistle', () => {
    spyOn(service, 'getWhistle').and.returnValue(audio);
    service.playWhistle();
    expect(audio.play).toHaveBeenCalled();
    service.stopWhistle();
    expect(audio.pause).toHaveBeenCalled();
  });

  it('stops a timed alarm and clears its timer', () => {
    jasmine.clock().install();
    try {
      spyOn(service, 'getAlarm').and.returnValue(audio);
      service.playAlarmByTime(2);
      jasmine.clock().tick(1001);
      expect(audio.pause).toHaveBeenCalled();
      service.ngOnDestroy();
    } finally {
      jasmine.clock().uninstall();
    }
  });
});
