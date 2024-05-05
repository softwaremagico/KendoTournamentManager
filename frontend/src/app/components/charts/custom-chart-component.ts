import {Component, OnInit} from "@angular/core";
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
import {ApexTheme, ChartType} from "ng-apexcharts/lib/model/apex-types";

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
      click?(chart?: any, options?: any, e?: any): any;
    }[];
  };
  export?: {
    csv?: {
      filename?: string;
      columnDelimiter?: string;
      headerCategory?: string;
      headerValue?: string;
      dateFormatter?(timestamp?: number): any;
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

@Component({
  template: ''
})
export abstract class CustomChartComponent implements OnInit {

  protected titleTextColor: string = "#000000"
  protected legendTextColor: string = "#000000"
  protected axisTextColor: string = "#000000"
  protected toolTextTipColor: string = "#000000"
  protected darkMode: boolean;

  protected constructor(protected darkModeService: DarkModeService, protected userSessionService: UserSessionService) {
    this.darkMode = userSessionService.getNightMode();
  }


  ngOnInit(): void {
    this.darkModeService.darkModeSwitched.subscribe((switched: boolean): void => {
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
      width: width,
      //height: height,
      type: type,
      dropShadow: this.getShadow(shadow),
      toolbar: this.getToolbar(showToolbar),
      background: 'transparent'
    }
  }

  protected getTitle(title: string | undefined, titleAlignment: "left" | "center" | "right" = "center"): ApexTitleSubtitle {
    return {
      text: title,
      align: titleAlignment,
      style: {
        fontSize: '14px',
        fontWeight: 'bold',
        fontFamily: 'Roboto',
        color: this.titleTextColor
      },
    }
  }

  protected getLegend(legendPosition: 'left' | 'bottom' | 'right' | 'top' = "bottom"): ApexLegend {
    return {
      position: legendPosition,
      labels: {
        colors: this.legendTextColor,
        useSeriesColors: false
      },
    }
  }

  protected getResponsive(legendPosition: 'left' | 'bottom' | 'right' | 'top' = "bottom"): ApexResponsive[] {
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

  protected getFill(fill: "gradient" | "solid" | "pattern" | "image" = "solid", opacity?: number): ApexFill {
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
      },
    }
  }

  protected getTheme(): ApexTheme {
    return {
      mode: this.darkMode ? 'dark' : 'light',
    }
  }

  protected abstract getPlotOptions(): ApexPlotOptions | undefined;
}
