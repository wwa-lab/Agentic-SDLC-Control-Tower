import type { RiskCategory, RiskSeverity } from './enums';

export interface RiskItem {
  readonly id: string;
  readonly category: RiskCategory;
  readonly severity: RiskSeverity;
  readonly title: string;
  readonly detail: string;
  readonly ageDays: number;
  readonly primaryAction: {
    readonly label: string;
    readonly url: string;
  };
  readonly skillAttribution?: {
    readonly skillName: string;
    readonly executionId: string;
  };
}

export interface TeamRiskRadar {
  readonly groups: Record<RiskCategory, ReadonlyArray<RiskItem>>;
  readonly lastRefreshed: string;
  readonly total: number;
}
