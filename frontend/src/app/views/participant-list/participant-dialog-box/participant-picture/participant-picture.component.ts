import {Component, OnInit} from '@angular/core';
import {RbacBasedComponent} from "../../../../components/RbacBasedComponent";
import {MatDialogRef} from "@angular/material/dialog";
import {RbacService} from "../../../../services/rbac/rbac.service";
import {WebcamImage} from "ngx-webcam";
import {Observable, Subject} from "rxjs";

@Component({
  selector: 'app-participant-picture',
  templateUrl: './participant-picture.component.html',
  styleUrls: ['./participant-picture.component.scss']
})
export class ParticipantPictureComponent extends RbacBasedComponent implements OnInit {
  private trigger: Subject<any> = new Subject();
  public webcamImage!: WebcamImage;
  private nextWebcam: Subject<any> = new Subject();
  sysImage = '';

  constructor(public dialogRef: MatDialogRef<ParticipantPictureComponent>, rbacService: RbacService,) {
    super(rbacService);
  }

  ngOnInit(): void {
  }

  public getSnapshot(): void {
    this.trigger.next(void 0);
  }

  public captureImg(webcamImage: WebcamImage): void {
    this.webcamImage = webcamImage;
    this.sysImage = webcamImage!.imageAsDataUrl;
    console.info('got webcam image', this.sysImage);
  }

  public get invokeObservable(): Observable<any> {
    return this.trigger.asObservable();
  }

  public get nextWebcamObservable(): Observable<any> {
    return this.nextWebcam.asObservable();
  }

}
