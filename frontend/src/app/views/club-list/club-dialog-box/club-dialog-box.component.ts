import {Component, Inject, Optional} from '@angular/core';
import {MAT_DIALOG_DATA, MatDialogRef} from '@angular/material/dialog';
import {Club} from "../../../models/club";
import {Action} from "../../../action";
import {RbacBasedComponent} from "../../../components/RbacBasedComponent";
import {RbacService} from "../../../services/rbac/rbac.service";
import {UntypedFormControl, UntypedFormGroup, Validators} from "@angular/forms";
import {RbacActivity} from "../../../services/rbac/rbac.activity";
import {InputLimits} from "../../../utils/input-limits";
import {CsvService} from "../../../services/csv-service";
import {MessageService} from "../../../services/message.service";
import {TranslocoService} from "@ngneat/transloco";

@Component({
  selector: 'app-club-dialog-box',
  templateUrl: './club-dialog-box.component.html',
  styleUrls: ['./club-dialog-box.component.scss']
})
export class ClubDialogBoxComponent extends RbacBasedComponent {

  protected CLUB_NAME_MIN_LENGTH: number = InputLimits.MIN_FIELD_LENGTH;
  protected CLUB_NAME_MAX_LENGTH: number = InputLimits.MAX_NORMAL_FIELD_LENGTH;
  protected CLUB_COUNTRY_MAX_LENGTH: number = InputLimits.MAX_SMALL_FIELD_LENGTH;
  protected CLUB_COUNTRY_MIN_LENGTH: number = InputLimits.MIN_FIELD_LENGTH;
  protected CLUB_CITY_MAX_LENGTH: number = InputLimits.MAX_SMALL_FIELD_LENGTH;
  protected CLUB_CITY_MIN_LENGTH: number = InputLimits.MIN_FIELD_LENGTH;
  protected CLUB_ADDRESS_MIN_LENGTH: number = InputLimits.MIN_FIELD_LENGTH;
  protected CLUB_ADDRESS_MAX_LENGTH: number = InputLimits.MAX_BIG_FIELD_LENGTH;
  protected CLUB_EMAIL_MAX_LENGTH: number = InputLimits.MAX_NORMAL_FIELD_LENGTH;
  protected CLUB_PHONE_MIN_LENGTH: number = InputLimits.MIN_FIELD_LENGTH;
  protected CLUB_PHONE_MAX_LENGTH: number = InputLimits.MAX_SMALL_FIELD_LENGTH;
  protected CLUB_WEB_MIN_LENGTH: number = InputLimits.MIN_FIELD_LENGTH;
  protected CLUB_WEB_MAX_LENGTH: number = InputLimits.MAX_NORMAL_FIELD_LENGTH;


  club: Club;
  title: string;
  action: Action;
  actionName: string;

  registerForm: UntypedFormGroup;

  constructor(
    public dialogRef: MatDialogRef<ClubDialogBoxComponent>, rbacService: RbacService, public csvService: CsvService,
    public messageService: MessageService, private translateService: TranslocoService,
    //@Optional() is used to prevent error if no data is passed
    @Optional() @Inject(MAT_DIALOG_DATA) public data: { title: string, action: Action, entity: Club }) {
    super(rbacService);
    this.club = data.entity;
    this.title = data.title;
    this.action = data.action;
    this.actionName = Action[data.action];

    this.registerForm = new UntypedFormGroup({
      clubName: new UntypedFormControl({
        value: this.club.name,
        disabled: !rbacService.isAllowed(RbacActivity.EDIT_TOURNAMENT)
      }, [Validators.required, Validators.minLength(this.CLUB_NAME_MIN_LENGTH), Validators.maxLength(this.CLUB_NAME_MAX_LENGTH)]),
      clubCountry: new UntypedFormControl({
        value: this.club.country,
        disabled: !rbacService.isAllowed(RbacActivity.EDIT_TOURNAMENT)
      }, [Validators.required, Validators.minLength(this.CLUB_COUNTRY_MIN_LENGTH), Validators.maxLength(this.CLUB_COUNTRY_MAX_LENGTH)]),
      clubCity: new UntypedFormControl({
        value: this.club.city,
        disabled: !rbacService.isAllowed(RbacActivity.EDIT_TOURNAMENT)
      }, [Validators.required, Validators.minLength(this.CLUB_CITY_MIN_LENGTH), Validators.maxLength(this.CLUB_CITY_MAX_LENGTH)]),
      clubAddress: new UntypedFormControl({
        value: this.club.address,
        disabled: !rbacService.isAllowed(RbacActivity.EDIT_TOURNAMENT)
      }, [Validators.minLength(this.CLUB_ADDRESS_MIN_LENGTH), Validators.maxLength(this.CLUB_ADDRESS_MAX_LENGTH)]),
      clubEmail: new UntypedFormControl({
        value: this.club.email,
        disabled: !rbacService.isAllowed(RbacActivity.EDIT_TOURNAMENT)
      }, [Validators.email, Validators.maxLength(this.CLUB_EMAIL_MAX_LENGTH)]),
      clubPhone: new UntypedFormControl({
        value: this.club.phone,
        disabled: !rbacService.isAllowed(RbacActivity.EDIT_TOURNAMENT)
      }, [Validators.pattern("[- +()0-9]+"), Validators.minLength(this.CLUB_PHONE_MIN_LENGTH), Validators.maxLength(this.CLUB_PHONE_MAX_LENGTH)]),
      clubWeb: new UntypedFormControl({
        value: this.club.web,
        disabled: !rbacService.isAllowed(RbacActivity.EDIT_TOURNAMENT)
      }, [Validators.pattern('(https?://)?([\\da-z.-]+)\\.([a-z.]{2,6})[/\\w .-]*/?'),
        Validators.minLength(this.CLUB_WEB_MIN_LENGTH), Validators.maxLength(this.CLUB_WEB_MAX_LENGTH)]),
    });
  }

  doAction() {
    this.club.name = this.registerForm.get('clubName')!.value;
    this.club.country = this.registerForm.get('clubCountry')!.value;
    this.club.city = this.registerForm.get('clubCity')!.value;
    this.club.address = this.registerForm.get('clubAddress')!.value;
    this.club.email = this.registerForm.get('clubEmail')!.value;
    this.club.phone = this.registerForm.get('clubPhone')!.value;
    this.club.web = this.registerForm.get('clubWeb')!.value;
    this.dialogRef.close({data: this.club, action: this.action});
  }

  closeDialog() {
    this.dialogRef.close({action: Action.Cancel});
  }

  handleFileInput(event: Event) {
    const element = event.currentTarget as HTMLInputElement;
    let fileList: FileList | null = element.files;
    if (fileList) {
      const file: File | null = fileList.item(0);
      if (file) {
        this.csvService.addClubs(file).subscribe(_clubs => {
          if (_clubs.length == 0) {
            this.messageService.infoMessage('clubStored');
            //We cancel action or will be saved later again.
            this.dialogRef.close({action: Action.Cancel});
          } else {
            const parameters: object = {element: _clubs[0].name};
            this.messageService.errorMessage(this.translateService.translate('failedOnCsvField', parameters));
          }
        });
      }
    }
  }
}
