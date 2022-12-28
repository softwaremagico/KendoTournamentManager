import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {RbacBasedComponent} from "../../../../components/RbacBasedComponent";
import {ImageService} from "../../../../services/image.service";
import {RbacService} from "../../../../services/rbac/rbac.service";
import {WebcamImage, WebcamInitError} from "ngx-webcam";
import {Observable, Subject} from 'rxjs';
import {Action} from "../../../../action";
import {MatDialogRef} from "@angular/material/dialog";
import {MessageService} from "../../../../services/message.service";

@Component({
  selector: 'app-participant-picture',
  templateUrl: './participant-picture-dialog-box.component.html',
  styleUrls: ['./participant-picture-dialog-box.component.scss']
})
export class ParticipantPictureDialogBoxComponent extends RbacBasedComponent implements OnInit {
  pictures: Array<string> = [];
  selectedPicture: number | undefined = undefined;

  @Output()
  public imageClicked = new EventEmitter<WebcamImage>();
  private pictureGenerated: Subject<void> = new Subject<void>();

  constructor(public dialogRef: MatDialogRef<ParticipantPictureDialogBoxComponent>, rbacService: RbacService,
              private imageService: ImageService, public messageService: MessageService) {
    super(rbacService);
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
    if (this.pictures.length > 0) {
      let imageData = this.pictures[this.pictures.length - 1].split('base64,');
      if (imageData.length > 1) {
        this.imageService.createBlobImageFileAndSave(imageData[1]);
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
