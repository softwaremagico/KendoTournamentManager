import {Injectable, OnDestroy} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class AudioService implements OnDestroy {

  private alarm: HTMLAudioElement;
  private whistle: HTMLAudioElement;
  private soundHandler: NodeJS.Timeout | null;

  ngOnDestroy(): void {
    if (this.soundHandler != null) {
      clearInterval(this.soundHandler);
      this.soundHandler = null;
    }
  }

  clearSound(): void {
    if (this.soundHandler != null) {
      clearInterval(this.soundHandler);
      this.soundHandler = null;
    }
  }

  getAlarm(): HTMLAudioElement {
    const audio = new Audio();
    audio.src = '../../assets/audio/alarm.wav';
    audio.volume = 1;
    audio.load();
    audio.muted = false;
    return audio;
  }

  getWhistle(): HTMLAudioElement {
    const audio = new Audio();
    audio.src = '../../assets/audio/whistle.wav';
    audio.volume = 1;
    audio.load();
    audio.muted = false;
    return audio;
  }

  playAlarmByTime(seconds: number): void {
    this.playAlarm();
    this.clearSound();
    this.soundHandler = setInterval((): void => {
      this.stopAlarm();
    }, (seconds - 1) * 1000);
  }

  playAlarm(): void {
    if (!this.alarm) {
      this.alarm = this.getAlarm();
    }
    this.alarm.muted = false;
    this.alarm.loop = true;
    this.alarm.play().then().catch(() => console.error('Cannot reproduce audio!'));
  }

  stopAlarm(): void {
    this.alarm.muted = true
    this.alarm.pause();
    this.alarm.load();
  }

  playWhistleByTime(seconds: number): void {
    this.playWhistle();
    this.clearSound();
    this.soundHandler = setInterval((): void => {
      this.stopWhistle();
    }, (seconds - 1) * 1000);
  }

  playWhistle(): void {
    if (!this.whistle) {
      this.whistle = this.getWhistle();
    }
    this.whistle.muted = false;
    this.whistle.loop = true;
    this.whistle.play().then().catch(() => console.error('Cannot reproduce audio!'));
  }

  stopWhistle(): void {
    this.whistle.muted = true
    this.whistle.pause();
    this.whistle.load();
  }

}
