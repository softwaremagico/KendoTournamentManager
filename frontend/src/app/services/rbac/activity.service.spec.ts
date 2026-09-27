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
  });
});
