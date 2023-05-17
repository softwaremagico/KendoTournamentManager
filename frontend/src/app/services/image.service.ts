import {Injectable, Input} from '@angular/core';
import {Observable, Observer} from "rxjs";
import {Participant} from "../models/participant";
import {NameUtilsService} from "./name-utils.service";

@Injectable({
  providedIn: 'root'
})
export class ImageService {

  @Input()
  participant: Participant;

  constructor(private nameUtilsService: NameUtilsService) {
  }

  createBlobImageFileAndSave(base64ImageUrl: string, extension: string = 'jpeg'): void {
    this.dataURItoBlob(base64ImageUrl, extension).subscribe((blob: Blob) => {
      this.generateFileName(extension);
    });
  }

  /* Method to convert Base64Data Url as Image Blob */
  dataURItoBlob(dataURI: string, extension: string): Observable<Blob> {
    return new Observable((observer: Observer<Blob>) => {
      const byteString: string = window.atob(dataURI);
      const arrayBuffer: ArrayBuffer = new ArrayBuffer(byteString.length);
      const int8Array: Uint8Array = new Uint8Array(arrayBuffer);
      for (let i = 0; i < byteString.length; i++) {
        int8Array[i] = byteString.charCodeAt(i);
      }
      const blob = new Blob([int8Array], {type: `image/${extension}`});
      observer.next(blob);
      observer.complete();
    });
  }

  generateFileName(ext: string): string {
    const date: number = new Date().valueOf();
    return date + "." + this.nameUtilsService.getLastnameNameNoSpaces(this.participant) + "." + ext;
  }
}
