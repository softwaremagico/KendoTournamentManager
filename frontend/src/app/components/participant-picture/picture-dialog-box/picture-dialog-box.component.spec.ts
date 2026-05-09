import { RbacService } from '../../../services/rbac/rbac.service';
import { PictureDialogBoxComponent } from './picture-dialog-box.component';

describe('PictureDialogBoxComponent', () => {
  let component: PictureDialogBoxComponent;
  let rbacServiceSpy: jasmine.SpyObj<RbacService>;

  beforeEach(() => {
    rbacServiceSpy = jasmine.createSpyObj('RbacService', ['isAllowed']);
    component = new PictureDialogBoxComponent(rbacServiceSpy);
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should set participantPicture input', () => {
    component.participantPicture = 'data:image/png;base64,abc';

    expect(component.participantPicture).toBe('data:image/png;base64,abc');
  });

  it('should emit onClosed when closeDialog is called', () => {
    spyOn(component.closed, 'emit');

    component.closeDialog();

    expect(component.closed.emit).toHaveBeenCalledOnceWith();
  });
});

