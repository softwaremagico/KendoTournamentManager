import {DatePipe} from "@angular/common";

export function convertSeconds(seconds: number | undefined): string {
  if (seconds) {
    const minutes = Math.floor(seconds / 60);
    if (minutes > 0) {
      return minutes + "m " + seconds % 60 + "s";
    }
    return seconds + "s";
  }
  return "";
}

export function convertDate(pipe: DatePipe, date: Date | undefined): string | null {
  if (date && new Date(date).getFullYear() > 2000) {
    return pipe.transform(date, 'short');
  }
  return "";
}
