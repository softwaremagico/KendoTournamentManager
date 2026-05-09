import { Duel } from '../../../../models/duel';
import { DrawComponent } from './draw.component';

describe('DrawComponent', () => {
  let component: DrawComponent;

  beforeEach(() => {
    component = new DrawComponent();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should allow setting duel input', () => {
    const duel = new Duel();
    duel.finished = false;
    duel.competitor1Score = [];
    duel.competitor2Score = [];

    component.duel = duel;

    expect(component.duel).toBe(duel);
  });
});

