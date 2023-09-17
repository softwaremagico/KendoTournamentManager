import {Group} from "./group";
import {Element} from "./element";

export class GroupLink extends Element {

  source: Group;

  destination: Group;

  winner: number;

  public static override copy(source: GroupLink, target: GroupLink): void {
    Element.copy(source, target);
    if (source.source !== undefined) {
      target.source = Group.clone(source.source);
    }
    if (source.destination !== undefined) {
      target.destination = Group.clone(source.destination);
    }
    target.winner = source.winner;
  }

  public static clone(data: GroupLink): GroupLink {
    const instance: GroupLink = new GroupLink();
    this.copy(data, instance);
    return instance;
  }
}
