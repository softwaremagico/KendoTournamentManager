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

