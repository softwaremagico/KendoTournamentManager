import {ComponentFixture, TestBed} from '@angular/core/testing';
import {CommonModule} from '@angular/common';
import {CUSTOM_ELEMENTS_SCHEMA, SimpleChange} from '@angular/core';

import {AchievementWallComponent} from './achievement-wall.component';
import {Achievement} from '../../models/achievement.model';
import {AchievementType} from '../../models/achievement-type.model';
import {AchievementGrade} from '../../models/achievement-grade.model';

describe('AchievementWallComponent', () => {
  let component: AchievementWallComponent;
  let fixture: ComponentFixture<AchievementWallComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [CommonModule],
      declarations: [AchievementWallComponent],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(AchievementWallComponent);
    component = fixture.componentInstance;
    component.view = 'participant';
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should group achievements by type and leave empty arrays for types without achievements', () => {
    const billy = createAchievement(AchievementType.BILLY_THE_KID);
    const terminator = createAchievement(AchievementType.TERMINATOR);
    const secondBilly = createAchievement(AchievementType.BILLY_THE_KID, AchievementGrade.GOLD);

    component.achievements = [billy, terminator, secondBilly];
    component.ngOnChanges({
      achievements: new SimpleChange(undefined, component.achievements, true)
    });

    expect(component.groupedAchievements.get(AchievementType.BILLY_THE_KID)).toEqual([billy, secondBilly]);
    expect(component.groupedAchievements.get(AchievementType.TERMINATOR)).toEqual([terminator]);
    expect(component.groupedAchievements.get(AchievementType.THE_KING)).toEqual([]);
  });

  it('should create empty groups for all types when it receives an empty list', () => {
    component.achievements = [];
    component.ngOnChanges({
      achievements: new SimpleChange(undefined, component.achievements, true)
    });
    fixture.detectChanges();

    expect(component.groupedAchievements.size).toBe(component.totalAchievementsTypes.length);
    expect([...component.groupedAchievements.values()].every((achievements) => achievements.length === 0)).toBeTrue();
    expect(fixture.nativeElement.querySelectorAll('achievement-tile').length).toBe(component.totalAchievementsTypes.length);
  });

  it('should recalculate groups when the achievements input changes', () => {
    component.achievements = [createAchievement(AchievementType.BILLY_THE_KID)];
    component.ngOnChanges({
      achievements: new SimpleChange(undefined, component.achievements, true)
    });

    const updatedAchievements = [createAchievement(AchievementType.THE_KING)];
    component.achievements = updatedAchievements;
    component.ngOnChanges({
      achievements: new SimpleChange([], updatedAchievements, false)
    });

    expect(component.groupedAchievements.get(AchievementType.BILLY_THE_KID)).toEqual([]);
    expect(component.groupedAchievements.get(AchievementType.THE_KING)).toEqual(updatedAchievements);
  });

  function createAchievement(type: AchievementType, grade: AchievementGrade = AchievementGrade.NORMAL): Achievement {
    const achievement = new Achievement();
    achievement.achievementType = type;
    achievement.achievementGrade = grade;
    return achievement;
  }
});

