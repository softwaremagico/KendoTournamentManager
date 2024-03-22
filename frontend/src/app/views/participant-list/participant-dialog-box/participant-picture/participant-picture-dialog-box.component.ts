import {Component, EventEmitter, Inject, OnInit, Optional, Output} from '@angular/core';
import {RbacBasedComponent} from "../../../../components/RbacBasedComponent";
import {ImageService} from "../../../../services/image.service";
import {RbacService} from "../../../../services/rbac/rbac.service";
import {WebcamImage, WebcamInitError} from "ngx-webcam";
import {Observable, Subject} from 'rxjs';
import {Action} from "../../../../action";
import {MAT_DIALOG_DATA, MatDialogRef} from "@angular/material/dialog";
import {MessageService} from "../../../../services/message.service";
import {FileService} from "../../../../services/file.service";
import {Participant} from "../../../../models/participant";
import {ParticipantImage} from "../../../../models/participant-image.model";
import {PictureUpdatedService} from "../../../../services/notifications/picture-updated.service";
import {TranslateService} from "@ngx-translate/core";
import {ImageFormat} from "../../../../models/image-format";

@Component({
  selector: 'app-participant-picture',
  templateUrl: './participant-picture-dialog-box.component.html',
  styleUrls: ['./participant-picture-dialog-box.component.scss']
})
export class ParticipantPictureDialogBoxComponent extends RbacBasedComponent implements OnInit {
  pictures: Array<string> = [];
  selectedPicture: number | undefined = undefined;
  participant: Participant;
  imageType: string = "image/png";
  modes: string[] = ["environment", "user"];
  facingMode: number = 0;  //Set back camera
  videoOptions: MediaTrackConstraints = {};
  availableCameras: number;

  @Output()
  public imageClicked = new EventEmitter<WebcamImage>();
  private pictureGenerated: Subject<void> = new Subject<void>();

  constructor(@Optional() @Inject(MAT_DIALOG_DATA) public data: { participant: Participant },
              public dialogRef: MatDialogRef<ParticipantPictureDialogBoxComponent>, rbacService: RbacService,
              private imageService: ImageService, public messageService: MessageService, private fileService: FileService,
              private translateService: TranslateService, private pictureUpdatedService: PictureUpdatedService) {
    super(rbacService);
    this.participant = data.participant;
  }

  ngOnInit(): void {
    this.selectCamera();
    navigator.mediaDevices.enumerateDevices().then((devices: MediaDeviceInfo[]): void => {
      this.availableCameras = devices.filter((device: MediaDeviceInfo): boolean => device.kind === "videoinput").length;
    }).catch(() => console.error('Cannot select camera!'));
  }

  closeDialog(): void {
    this.dialogRef.close({action: Action.Cancel});
  }

  public takePicture(): void {
    this.pictureGenerated.next();
    this.selectedPicture = this.pictures.length - 1;
  }

  public switchCamera(): void {
    this.facingMode = (this.facingMode + 1) % this.modes.length;
    this.selectCamera();
  }

  private selectCamera(): void {
    this.videoOptions.facingMode = {ideal: this.modes[this.facingMode]};
  }

  public errorHandler(error: WebcamInitError): void {
    this.messageService.errorMessage(error.message);
  }

  public pictureHandler(webcamImage: WebcamImage): void {
    this.pictures.push(webcamImage.imageAsDataUrl);
    //Only store last 5 pictures.
    if (this.pictures.length > 5) {
      this.pictures.splice(0, 1);
    }
    this.imageClicked.emit(webcamImage);
  }

  public saveImage(): void {
    if (this.pictures.length > 0 && this.selectedPicture !== undefined) {
      const imageDataAsBase64: string = this.pictures[this.selectedPicture];
      if (imageDataAsBase64.length > 1) {
        const image: ParticipantImage = new ParticipantImage();
        image.imageFormat = ImageFormat.BASE64;
        image.participant = this.participant;
        image.base64 = imageDataAsBase64;
        this.fileService.setBase64Picture(image).subscribe((_picture: ParticipantImage): void => {
          this.messageService.infoMessage('infoPictureStored');
          this.pictureUpdatedService.isPictureUpdated.next(_picture.base64);
          this.closeDialog();
        });
      }
    }
  }

  public get clickOnCamera(): Observable<void> {
    return this.pictureGenerated.asObservable();
  }

  selectImage(index: number): void {
    if (this.selectedPicture !== index) {
      this.selectedPicture = index;
    } else {
      this.selectedPicture = undefined;
    }
  }

  handleFileInput(event: Event): void {
    const element: HTMLInputElement = event.currentTarget as HTMLInputElement;
    let fileList: FileList | null = element.files;
    if (fileList) {
      const file: File | null = fileList.item(0);
      if (!file || file.size < 4096 || file.size > 2097152) {
        const parameters: object = {minSize: '4096', maxSize: '' + (2097152 / (1024 * 1024))};
        this.translateService.get('invalidFileSize', parameters).subscribe((res: string): void => {
          this.messageService.errorMessage(res);
        });
      } else {
        this.fileService.setParticipantFilePicture(file, this.participant).subscribe((_picture: ParticipantImage): void => {
          this.messageService.infoMessage('infoPictureStored');
          this.pictureUpdatedService.isPictureUpdated.next(_picture.base64);
          this.closeDialog();
        });
      }
    }
  }
}
