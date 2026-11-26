import type {ApexChart} from 'ng-apexcharts';

export type ChartType = NonNullable<ApexChart['type']>;

export interface ApexTheme {
  mode?: 'light' | 'dark';
  monochrome?: {
    enabled?: boolean;
    color?: string;
    shadeTo?: 'light' | 'dark';
    shadeIntensity?: number;
  };
  palette?: string;
}


export type LegendPosition = 'left' | 'bottom' | 'right' | 'top';
export type TitleAlignment = 'left' | 'center' | 'right';
export type ChartFill = 'gradient' | 'solid' | 'pattern' | 'image';
