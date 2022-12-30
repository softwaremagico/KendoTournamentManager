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
import {ImageFormat} from "../../../../models/image-format";
import {PictureUpdatedService} from "../../../../services/notifications/picture-updated.service";

@Component({
  selector: 'app-participant-picture',
  templateUrl: './participant-picture-dialog-box.component.html',
  styleUrls: ['./participant-picture-dialog-box.component.scss']
})
export class ParticipantPictureDialogBoxComponent extends RbacBasedComponent implements OnInit {
  pictures: Array<string> = [];
  selectedPicture: number | undefined = undefined;
  participant: Participant;

  @Output()
  public imageClicked = new EventEmitter<WebcamImage>();
  private pictureGenerated: Subject<void> = new Subject<void>();

  constructor(@Optional() @Inject(MAT_DIALOG_DATA) public data: { participant: Participant },
              public dialogRef: MatDialogRef<ParticipantPictureDialogBoxComponent>, rbacService: RbacService,
              private imageService: ImageService, public messageService: MessageService, private fileService: FileService,
              private pictureUpdatedService: PictureUpdatedService) {
    super(rbacService);
    this.participant = data.participant;
  }

  ngOnInit(): void {

  }

  closeDialog() {
    this.dialogRef.close({action: Action.Cancel});
  }

  public takePicture(): void {
    this.pictureGenerated.next();
    this.selectedPicture = this.pictures.length - 1;
  }

  public errorHandler(error: WebcamInitError): void {
    this.messageService.errorMessage(error.message);
  }

  public pictureHandler(webcamImage: WebcamImage): void {
    this.pictures.push(webcamImage.imageAsDataUrl);
    this.imageClicked.emit(webcamImage);
  }

  public saveImage() {
    if (this.pictures.length > 0 && this.selectedPicture !== undefined) {
      const imageDataAsBase64: string = this.pictures[this.selectedPicture];
      if (imageDataAsBase64.length > 1) {
        const image: ParticipantImage = new ParticipantImage();
        image.imageFormat = ImageFormat.BASE64;
        image.participant = this.participant;
        image.base64 = imageDataAsBase64;
        this.fileService.addPicture(image).subscribe(_picture => {
          this.messageService.infoMessage('infoPictureStored');
          this.pictureUpdatedService.isPictureUpdated.next(_picture!.base64);
          this.closeDialog();
        });
      }
    }
  }

  public get clickOnCamera(): Observable<void> {
    return this.pictureGenerated.asObservable();
  }

  selectImage(index: number) {
    if (this.selectedPicture !== index) {
      this.selectedPicture = index;
    } else {
      this.selectedPicture = undefined;
    }
  }
}
