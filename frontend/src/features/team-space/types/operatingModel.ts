import type { Lineage } from '@/shared/types/lineage';
import type {
  AccountableArea,
  AiAutonomyLevel,
  ApprovalMode,
  OperatingMode,
} from './enums';

export interface TeamOperatingModelField<T> {
  readonly value: T;
  readonly lineage: Lineage;
}

export interface OncallOwner {
  readonly memberId: string;
  readonly displayName: string;
  readonly rotationRef: string;
}

export interface AccountableOwner {
  readonly area: AccountableArea;
  readonly memberId: string;
  readonly displayName: string;
}

export interface TeamOperatingModel {
  readonly operatingMode: TeamOperatingModelField<OperatingMode>;
  readonly approvalMode: TeamOperatingModelField<ApprovalMode>;
  readonly aiAutonomyLevel: TeamOperatingModelField<AiAutonomyLevel>;
  readonly oncallOwner: TeamOperatingModelField<OncallOwner>;
  readonly accountableOwners: ReadonlyArray<AccountableOwner>;
  readonly platformCenterLink: {
    readonly url: string;
    readonly enabled: boolean;
  };
}
