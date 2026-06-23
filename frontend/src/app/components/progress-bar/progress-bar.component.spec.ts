import {ProgressBarComponent} from './progress-bar.component';

describe('ProgressBarComponent', () => {
  let component: ProgressBarComponent;

  beforeEach(() => {
    component = new ProgressBarComponent();
  });

  afterEach(() => {
    if (component.intervalId) {
      clearInterval(component.intervalId);
    }
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should have default input values', () => {
    expect(component.text).toBeUndefined();
    expect(component.hint).toBeUndefined();
    expect(component.percentage).toBe(50);
    expect(component.barIcon).toBe('attack');
    expect(component.drawnPercentage).toBe(0);
  });

  it('should increment drawnPercentage on ngOnInit', (done) => {
    component.percentage = 10;
    component.ngOnInit();

    setTimeout(() => {
      expect(component.drawnPercentage).toBeGreaterThan(0);
      done();
    }, 100);
  });

  it('should not exceed the percentage value', (done) => {
    component.percentage = 25;
    component.ngOnInit();

    setTimeout(() => {
      expect(component.drawnPercentage).toBeLessThanOrEqual(component.percentage);
      done();
    }, 200);
  });

  it('should reach the target percentage', (done) => {
    component.percentage = 5;
    component.ngOnInit();

    setTimeout(() => {
      expect(component.drawnPercentage).toBe(component.percentage);
      done();
    }, 300);
  });

  it('should stop incrementing when reaching the percentage', (done) => {
    component.percentage = 3;
    component.ngOnInit();

    setTimeout(() => {
      const finalValue = component.drawnPercentage;
      expect(finalValue).toBe(3);

      setTimeout(() => {
        expect(component.drawnPercentage).toBe(finalValue);
        done();
      }, 100);
    }, 200);
  });

  it('should clear the interval when progressInLoading is called at 100%', () => {
    spyOn(window, 'clearInterval');
    component.intervalId = setInterval(() => {}, 50);
    component.drawnPercentage = 100;

    component.progressInLoading();

    expect(window.clearInterval).toHaveBeenCalled();
  });

  it('should not clear the interval when progressInLoading is called before 100%', () => {
    spyOn(window, 'clearInterval');
    component.intervalId = setInterval(() => {}, 50);
    component.drawnPercentage = 50;

    component.progressInLoading();

    expect(window.clearInterval).not.toHaveBeenCalled();
  });

  it('should handle zero percentage', (done) => {
    component.percentage = 0;
    component.ngOnInit();

    setTimeout(() => {
      expect(component.drawnPercentage).toBe(0);
      done();
    }, 100);
  });

  it('should handle high percentage values', (done) => {
    component.percentage = 95;
    component.ngOnInit();

    setTimeout(() => {
      expect(component.drawnPercentage).toBeLessThanOrEqual(95);
      expect(component.drawnPercentage).toBeGreaterThan(0);
      done();
    }, 200);
  });
});

