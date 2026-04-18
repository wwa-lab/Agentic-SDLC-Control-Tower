import type { MilestoneStatus } from './enums';

export interface MilestoneOwner {
  readonly memberId: string;
  readonly displayName: string;
}

export interface Milestone {
  readonly id: string;
  readonly label: string;
  readonly targetDate: string;
  readonly status: MilestoneStatus;
  readonly percentComplete: number | null;
  readonly owner: MilestoneOwner | null;
  readonly isCurrent: boolean;
  readonly slippageReason: string | null;
}

export interface MilestoneHub {
  readonly milestones: ReadonlyArray<Milestone>;
  readonly projectManagementLink: {
    readonly url: string;
    readonly enabled: boolean;
  };
}
