import type { RiskCategory, RiskSeverity } from './enums';

export interface RiskOwner {
  readonly memberId: string;
  readonly displayName: string;
}

export interface RiskItem {
  readonly id: string;
  readonly title: string;
  readonly severity: RiskSeverity;
  readonly category: RiskCategory;
  readonly owner: RiskOwner | null;
  readonly ageDays: number;
  readonly latestNote: string | null;
  readonly primaryAction: {
    readonly label: string;
    readonly url: string;
  };
  readonly skillAttribution?: {
    readonly skillName: string;
    readonly executionId: string;
  };
}

export interface RiskRegistry {
  readonly items: ReadonlyArray<RiskItem>;
  readonly total: number;
  readonly lastRefreshed: string;
}
