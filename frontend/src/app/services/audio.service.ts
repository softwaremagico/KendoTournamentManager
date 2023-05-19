import {Injectable} from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class AudioService {

  playAlarm(): void {
    const audio = new Audio();
    audio.src = '../../assets/audio/alarm.wav';
    audio.load();
    audio.play().then().catch(() => console.error('Cannot reproduce audio!'));
  }
}
