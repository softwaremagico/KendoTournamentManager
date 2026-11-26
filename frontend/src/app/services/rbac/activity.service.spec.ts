import {ActivityService} from './activity.service';
import {RbacActivity} from './rbac.activity';
import {UserRoles} from './user-roles';

describe('ActivityService', () => {
  it('grants all admin activities to super administrators', () => {
    const service = new ActivityService();

    service.setRoles([UserRoles.SUPER_ADMIN]);

    expect(service.isAllowed(RbacActivity.CREATE_USER)).toBeTrue();
    expect(service.isAllowed(RbacActivity.DELETE_TOURNAMENT)).toBeTrue();
    expect(service.isAllowed(RbacActivity.UPDATE_OTHERS_PASSWORD)).toBeTrue();
    expect(service.isAllowed(RbacActivity.CHANGE_OTHERS_PASSWORD)).toBeTrue();
  });

  it('does not allow editors to change other users passwords', () => {
    const service = new ActivityService();

    service.setRoles([UserRoles.EDITOR]);

    expect(service.isAllowed(RbacActivity.CHANGE_OTHERS_PASSWORD)).toBeFalse();
    expect(service.isAllowed(RbacActivity.CHANGE_PASSWORD)).toBeTrue();
  });

  it('grants read-only activities to viewers and guests', () => {
    const service = new ActivityService();

    service.setRoles([UserRoles.VIEWER, UserRoles.GUEST]);

    expect(service.hasRoles()).toBeTrue();
    expect(service.isAllowed(RbacActivity.READ_ALL_TOURNAMENTS)).toBeTrue();
    expect(service.isAllowed(RbacActivity.READ_ONE_TOURNAMENT)).toBeTrue();
    expect(service.isAllowed(RbacActivity.DOWNLOAD_PDF)).toBeTrue();
    expect(service.isAllowed(RbacActivity.CREATE_USER)).toBeFalse();
    expect((service as any).activities).toContain(RbacActivity.WIKI_ACCESS);

    service.clear();
    expect(service.hasRoles()).toBeFalse();
    expect(service.isAllowed(RbacActivity.CREATE_USER)).toBeFalse();
    expect(service.isAllowed(undefined)).toBeFalse();
  });
});
