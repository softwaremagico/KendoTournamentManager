import {Directive, Input, OnInit} from "@angular/core";
import {Colors} from "./colors";
import {DarkModeService} from "../../services/notifications/dark-mode.service";
import {UserSessionService} from "../../services/user-session.service";
import {
  ApexChart,
  ApexDataLabels,
  ApexFill,
  ApexLegend,
  ApexMarkers,
  ApexPlotOptions,
  ApexResponsive,
  ApexStroke,
  ApexTitleSubtitle,
  ApexTooltip,
  ApexXAxis,
  ApexYAxis
} from "ng-apexcharts";
import {ApexTheme, ChartFill, ChartType, LegendPosition, TitleAlignment} from "./apex-types";
import {KendoComponent} from "../kendo-component";
import {takeUntil} from "rxjs";

interface ApexToolBar {
  show?: boolean;
  offsetX?: number;
  offsetY?: number;
  tools?: {
    download?: boolean | string;
    selection?: boolean | string;
    zoom?: boolean | string;
    zoomin?: boolean | string;
    zoomout?: boolean | string;
    pan?: boolean | string;
    reset?: boolean | string;
      customIcons?: {
      icon?: string;
      title?: string;
      index?: number;
      class?: string;
        click?(chart?: unknown, options?: unknown, e?: Event): void;
    }[];
  };
  export?: {
    csv?: {
      filename?: string;
      columnDelimiter?: string;
      headerCategory?: string;
      headerValue?: string;
      dateFormatter?(timestamp?: number): string;
    };
    svg?: {
      filename?: string;
    };
    png?: {
      filename?: string;
    };
  };
  autoSelected?: "zoom" | "selection" | "pan";
}

interface ApexDropShadow {
  enabled?: boolean;
  top?: number;
  left?: number;
  blur?: number;
  opacity?: number;
  color?: string;
}

@Directive()
export abstract class CustomChartComponent extends KendoComponent implements OnInit {

  protected titleTextColor: string = "#000000"
  protected legendTextColor: string = "#000000"
  protected axisTextColor: string = "#000000"
  protected toolTextTipColor: string = "#000000"
  protected darkMode: boolean;

  /** Ajustes compartidos por todas las gráficas, heredados como `@Input`. */
  @Input()
  public width: number = 500;
  @Input()
  public height: number | undefined = undefined;
  @Input()
  public showToolbar: boolean = false;
  @Input()
  public colors: string[] = Colors.defaultPalette;
  @Input()
  public title: string | undefined = undefined;
  @Input()
  public titleAlignment: TitleAlignment = "center";
  @Input()
  public fill: ChartFill = "solid";
  @Input()
  public shadow: boolean = true;
  @Input()
  public legendPosition: LegendPosition = "bottom";

  constructor(protected darkModeService: DarkModeService, protected userSessionService: UserSessionService) {
    super();
    this.darkMode = userSessionService.getNightMode();
    if (this.darkMode) {
      this.colors = Colors.defaultPaletteNightMode;
    }
  }

  /** Opciones comunes de cabecera, relleno, tooltip, título, leyenda y tema. */
  protected getCommonChartOptions(type: ChartType, fillOpacity?: number) {
    return {
      colors: this.colors,
      chart: this.getChart(type, this.width, this.height, this.shadow, this.showToolbar),
      fill: this.getFill(this.fill, fillOpacity),
      tooltip: this.getTooltip(),
      title: this.getTitle(this.title, this.titleAlignment),
      legend: this.getLegend(this.legendPosition),
      theme: this.getTheme()
    };
  }


  ngOnInit(): void {
    this.darkModeService.darkModeSwitched.pipe(takeUntil(this.destroySubject)).subscribe((switched: boolean): void => {
      this.darkMode = switched;
      this.setFontColors(switched);
      this.setProperties();
    });
    this.setFontColors(this.userSessionService.getNightMode());
    this.setProperties();
  }

  setFontColors(darkMode: boolean): void {
    this.titleTextColor = darkMode ? "#ffffff" : "#000000";
    this.legendTextColor = darkMode ? "#ffffff" : "#000000";
    this.axisTextColor = darkMode ? "#ffffff" : "#000000";
    this.toolTextTipColor = darkMode ? "#ffffff" : "#000000";
  }

  protected abstract setProperties(): void;

  protected getChart(type: ChartType, width: number, height: number | undefined, shadow: boolean, showToolbar: boolean): ApexChart {
    return {
      fontFamily: 'Montserrat',
      width: width,
      height: height ?? width,
      type: type,
      dropShadow: this.getShadow(shadow),
      toolbar: this.getToolbar(showToolbar),
      background: 'transparent'
    }
  }

  protected getTitle(title: string | undefined, titleAlignment: TitleAlignment = "center"): ApexTitleSubtitle {
    return {
      text: title?.toUpperCase(),
      align: titleAlignment,
      style: {
        fontSize: '20px',
        fontWeight: 'bold',
        fontFamily: 'Montserrat',
        color: this.titleTextColor
      },
    }
  }

  protected getLegend(legendPosition: LegendPosition = "bottom"): ApexLegend {
    return {
      position: legendPosition,
      labels: {
        colors: this.legendTextColor,
        useSeriesColors: false
      },
    }
  }

  protected getResponsive(legendPosition: LegendPosition = "bottom"): ApexResponsive[] {
    return [
      {
        breakpoint: 480,
        options: {
          chart: {
            width: 200
          },
          legend: {
            position: legendPosition
          }
        }
      }
    ];
  }

  protected getFill(fill: ChartFill = "solid", opacity?: number): ApexFill {
    return {
      type: fill,
      opacity: opacity,
      gradient: {
        shade: "light",
        shadeIntensity: 0.4,
        inverseColors: false,
        opacityFrom: 1,
        opacityTo: 1,
        stops: [0, 50, 53, 91]
      }
    }
  }

  /** Asigna la paleta en orden cíclico a una serie de datos. */
  protected setSeriesColors<T extends { color: string }>(data: T[]): T[] {
    for (let i = 0; i < data.length; i++) {
      data[i].color = this.colors[i % this.colors.length];
    }
    return data;
  }

  protected getShadow(shadow: boolean): ApexDropShadow {
    return {
      enabled: shadow,
      color: '#000',
      top: -5,
      left: 7,
      blur: 8,
      opacity: 0.2
    }
  }

  protected getToolbar(showToolbar: boolean): ApexToolBar {
    return {
      show: showToolbar,
    }
  }

  protected getLabels(showValuesLabels: boolean): ApexDataLabels {
    return {
      enabled: showValuesLabels,
      style: {
        fontFamily: 'Roboto',
        fontSize: '12px'
      },
    }
  }

  protected getXAxis(labels: string [], xAxisOnTop?: 'top' | 'bottom', title?: string | undefined): ApexXAxis {
    return {
      categories: labels,
      position: xAxisOnTop,
      title: {
        text: title
      },
      labels: {
        style: {
          fontFamily: 'Roboto',
          colors: this.axisTextColor
        },
      },
    }
  }

  protected getYAxis(showYAxis: boolean, title: string | undefined): ApexYAxis {
    return {
      show: showYAxis,
      title: {
        text: title
      },
      labels: {
        style: {
          fontFamily: 'Roboto',
          colors: this.axisTextColor
        },
      },
    }
  }

  protected getStroke(strokeWidth: number, curve: "straight" | "smooth" | "stepline" = "smooth"): ApexStroke {
    return {
      curve: curve,
      width: strokeWidth,
    }
  }

  protected getMarkers(): ApexMarkers {
    return {
      size: 0
    }
  }

  protected getTooltip(): ApexTooltip {
    return {
      theme: this.darkMode ? 'dark' : 'light',
      //fillSeriesColor: true,
      style: {
        fontFamily: 'Roboto',
      }
    }
  }

  protected getTheme(): ApexTheme {
    return {
      mode: this.darkMode ? 'dark' : 'light',
    }
  }

  protected abstract getPlotOptions(): ApexPlotOptions | undefined;

  convertToMultiline(text: string, lineLength: number): string[] {
    const symbolWidth: number = 10;
    const chartWidth: number = 300;
    const rows = Math.ceil(lineLength / (text.length * symbolWidth));
    const newText: string[] = [];
    const symbolQuantity = Math.floor(chartWidth / symbolWidth);

    for (let i = 0; i < rows; i++) {
      newText.push(text.substring(symbolQuantity * i, symbolQuantity * (i + 1)));
    }
    return newText;
  }
}
