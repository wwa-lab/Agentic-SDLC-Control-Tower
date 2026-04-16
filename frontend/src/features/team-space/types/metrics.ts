import type { MetricKey, MetricUnit, TrendDirection } from './enums';

export interface TeamMetricItem {
  readonly key: MetricKey;
  readonly label: string;
  readonly currentValue: number;
  readonly previousValue: number;
  readonly unit: MetricUnit;
  readonly trend: TrendDirection;
  readonly historyLink: string;
  readonly tooltip: string;
}

export interface TeamMetrics {
  readonly deliveryEfficiency: ReadonlyArray<TeamMetricItem>;
  readonly quality: ReadonlyArray<TeamMetricItem>;
  readonly stability: ReadonlyArray<TeamMetricItem>;
  readonly governanceMaturity: ReadonlyArray<TeamMetricItem>;
  readonly aiParticipation: ReadonlyArray<TeamMetricItem>;
  readonly lastRefreshed: string;
}
