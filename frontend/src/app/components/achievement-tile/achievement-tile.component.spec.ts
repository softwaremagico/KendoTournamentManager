import { SimpleChange } from '@angular/core';
import { of } from 'rxjs';
import { TranslocoService } from '@ngneat/transloco';
import { registerLocaleData } from '@angular/common';
import localeEs from '@angular/common/locales/es';
import { AchievementTileComponent } from './achievement-tile.component';
import { Achievement } from '../../models/achievement.model';
import { AchievementGrade } from '../../models/achievement-grade.model';
import { AchievementType } from '../../models/achievement-type.model';
import { NameUtilsService } from '../../services/name-utils.service';
import { AchievementsService } from '../../services/achievements.service';

registerLocaleData(localeEs, 'es-ES');

describe('AchievementTileComponent', () => {
  let component: AchievementTileComponent;
  let translocoServiceSpy: jasmine.SpyObj<TranslocoService>;
  let nameUtilsServiceSpy: jasmine.SpyObj<NameUtilsService>;
  let achievementsServiceSpy: jasmine.SpyObj<AchievementsService>;

  const createAchievement = (
    grade: AchievementGrade,
    daysAgo: number,
    type: AchievementType = AchievementType.BILLY_THE_KID
  ): Achievement => {
    const createdAt = new Date();
    createdAt.setDate(createdAt.getDate() - daysAgo);

    return {
      achievementGrade: grade,
      achievementType: type,
      createdAt,
      participant: {
        id: 11,
        name: 'John',
        lastname: 'Doe'
      },
      tournament: {
        name: 'Open Cup',
        createdAt: new Date('2026-01-01')
      }
    } as unknown as Achievement;
  };

  beforeEach(() => {
    translocoServiceSpy = jasmine.createSpyObj('TranslocoService', ['translate']);
    nameUtilsServiceSpy = jasmine.createSpyObj('NameUtilsService', ['getDisplayName']);
    achievementsServiceSpy = jasmine.createSpyObj('AchievementsService', ['countByType']);

    (translocoServiceSpy.translate as jasmine.Spy).and.callFake((key: string, params?: { totalParticipants?: number }) => {
      if (params?.totalParticipants !== undefined) {
        return `${key}:${params.totalParticipants}`;
      }
      return key;
    });
    nameUtilsServiceSpy.getDisplayName.and.returnValue('John Doe');
    achievementsServiceSpy.countByType.and.returnValue(of(5));

    component = new AchievementTileComponent(
      translocoServiceSpy,
      nameUtilsServiceSpy,
      achievementsServiceSpy
    );
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should set grade to NORMAL on init', () => {
    component.ngOnInit();

    expect(component.grade).toBe(AchievementGrade.NORMAL);
  });

  it('should return locked image when there are no achievements', () => {
    component.achievements = [];

    expect(component.getAchievementImage()).toBe('assets/achievements/locked.svg');
  });

  it('should return image by achievement type when achievements exist', () => {
    component.achievements = [createAchievement(AchievementGrade.BRONZE, 1)];
    component.achievementType = AchievementType.TERMINATOR;

    expect(component.getAchievementImage()).toBe('assets/achievements/terminator.svg');
  });

  it('should return false for new achievement in tournament view', () => {
    component.view = 'tournament';
    component.achievements = [createAchievement(AchievementGrade.NORMAL, 0)];

    expect(component.isNewAchievement()).toBeFalse();
  });

  it('should return true for new achievement in participant view', () => {
    component.view = 'participant';
    component.achievements = [createAchievement(AchievementGrade.NORMAL, 0)];

    expect(component.isNewAchievement()).toBeTrue();
  });

  it('should return total achievements length', () => {
    component.achievements = [
      createAchievement(AchievementGrade.NORMAL, 0),
      createAchievement(AchievementGrade.BRONZE, 3)
    ];

    expect(component.getTotalAchievements()).toBe(2);
  });

  it('should build tournament total text with translated key', () => {
    component.view = 'tournament';

    const text = component.totalAchievedByText(9);

    expect(text).toContain('achievementToolTipTotal:9');
  });

  it('should build participant total text subtracting one participant', () => {
    component.view = 'participant';

    const text = component.totalAchievedByText(9);

    expect(text).toContain('achievementToolTipOthersTotal:8');
  });

  it('should set GOLD grade and load totalHtml on achievements change', () => {
    component.view = 'participant';
    component.ngOnInit();
    component.achievements = [
      createAchievement(AchievementGrade.BRONZE, 1, AchievementType.TERMINATOR),
      createAchievement(AchievementGrade.GOLD, 1, AchievementType.TERMINATOR)
    ];

    component.ngOnChanges({
      achievements: new SimpleChange(undefined, component.achievements, true)
    });

    expect(component.grade).toBe(AchievementGrade.GOLD);
    expect(component.totalAchievements).toBe(2);
    expect(achievementsServiceSpy.countByType).toHaveBeenCalledOnceWith(AchievementType.TERMINATOR);
    expect(component.totalHtml).toContain('achievementToolTipOthersTotal:4');
  });

  it('should build tournament tooltip with participant display names', () => {
    component.view = 'tournament';
    component.achievements = [createAchievement(AchievementGrade.SILVER, 1)];

    const tooltip = component.tooltipText();

    expect(nameUtilsServiceSpy.getDisplayName).toHaveBeenCalled();
    expect(tooltip).toContain('achievementToolTipObtainedBy');
    expect(tooltip).toContain('John Doe');
  });

  it('should update tooltip border flags based on mouse position', () => {
    Object.defineProperty(window, 'innerWidth', { configurable: true, value: 800 });
    Object.defineProperty(window, 'innerHeight', { configurable: true, value: 600 });

    component.updateCoordinates({ clientX: 10, clientY: 500 } as MouseEvent);

    expect(component.onLeftBorder).toBeTrue();
    expect(component.onRightBorder).toBeFalse();
    expect(component.onBottomBorder).toBeTrue();
  });

  it('should clear coordinates when clearCoordinates is called', () => {
    component.mouseX = 99;
    component.mouseY = 77;

    component.clearCoordinates();

    expect(component.mouseX).toBeUndefined();
    expect(component.mouseY).toBeUndefined();
  });
});



