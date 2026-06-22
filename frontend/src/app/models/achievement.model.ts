import {Element} from "./element";
import {Participant} from "./participant";
import {Tournament} from "./tournament";
import {AchievementType} from "./achievement-type.model";
import {AchievementGrade} from "./achievement-grade.model";

export class Achievement extends Element {

  public participant: Participant;
  public tournament: Tournament;
  public achievementType: AchievementType;
  public achievementGrade: AchievementGrade;

  public static override copy(source: Achievement, target: Achievement): void {
    Element.copy(source, target);
    if (source.participant !== undefined) {
      target.participant = Participant.clone(source.participant);
    }
    if (source.tournament !== undefined) {
      target.tournament = Tournament.clone(source.tournament);
    }
    target.achievementType = source.achievementType;
    target.achievementGrade = source.achievementGrade;
  }

  public static clone(data: Achievement): Achievement {
    const instance: Achievement = new Achievement();
    this.copy(data, instance);
    return instance;
  }

}
