import {Directive} from "@angular/core";
import {KendoComponent} from "../kendo-component";

/**
 * Estado y cálculo de la posición del tooltip compartidos por las fichas de
 * logros y los componentes de puntuación y falta. Mantiene los umbrales y los
 * nombres de las marcas usados por las plantillas.
 */
@Directive()
export abstract class TooltipPositionedComponent extends KendoComponent {

  mouseX: number | undefined;
  mouseY: number | undefined;
  screenHeight: number | undefined;
  screenWidth: number | undefined;
  onLeftBorder: boolean;
  onRightBorder: boolean;
  onBottomBorder: boolean;

  updateCoordinates($event: MouseEvent): void {
    this.mouseX = $event.clientX;
    this.mouseY = $event.clientY;
    this.calculateTooltipMargin();
  }

  clearCoordinates($event?: MouseEvent): void {
    this.mouseX = undefined;
    this.mouseY = undefined;
  }

  calculateTooltipMargin(): void {
    this.screenHeight = window.innerHeight;
    this.screenWidth = window.innerWidth;
    this.onLeftBorder = false;
    this.onRightBorder = false;
    if (this.mouseX! - 150 < 0) {
      this.onLeftBorder = true;
    }
    if (this.mouseX! + 150 > this.screenWidth) {
      this.onRightBorder = true;
    }
    if (this.mouseY! + 250 > this.screenHeight) {
      this.onBottomBorder = true;
    }
  }
}
