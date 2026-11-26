import {CookieService} from 'ngx-cookie-service';
import {of} from 'rxjs';
import {UserService} from '../user.service';
import {ActivityService} from './activity.service';
import {RbacActivity} from './rbac.activity';
import {RbacService} from './rbac.service';
import {UserRoles} from './user-roles';

describe('RbacService', () => {
  it('loads roles for an authenticated session and delegates activity checks', () => {
    const users = jasmine.createSpyObj<UserService>('UserService', ['getRoles']);
    const cookies = jasmine.createSpyObj<CookieService>('CookieService', ['get']);
    const activities = jasmine.createSpyObj<ActivityService>('ActivityService', ['setRoles', 'isAllowed'], {activities: []});
    users.getRoles.and.returnValue(of([UserRoles.VIEWER]));
    cookies.get.and.returnValue('token');
    activities.isAllowed.and.returnValue(true);
    const service = new RbacService(users, cookies, activities);
    expect(activities.setRoles).toHaveBeenCalledWith([UserRoles.VIEWER]);
    expect(service.isAllowed(undefined)).toBeTrue();
    expect(activities.isAllowed).toHaveBeenCalledWith(undefined);
    expect(service.getActivities()).toEqual([]);
  });

  it('does not request roles when no session cookie is present', () => {
    const users = jasmine.createSpyObj<UserService>('UserService', ['getRoles']);
    const cookies = jasmine.createSpyObj<CookieService>('CookieService', ['get']);
    const activities = jasmine.createSpyObj<ActivityService>('ActivityService', ['setRoles', 'isAllowed']);
    cookies.get.and.returnValue('');
    new RbacService(users, cookies, activities);
    expect(users.getRoles).not.toHaveBeenCalled();
  });
});
