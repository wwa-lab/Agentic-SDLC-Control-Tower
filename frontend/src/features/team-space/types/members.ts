import type { CoverageGapKind, OncallStatus } from './enums';

export interface MemberMatrixRow {
  readonly memberId: string;
  readonly displayName: string;
  readonly roles: ReadonlyArray<string>;
  readonly oncallStatus: OncallStatus;
  readonly keyPermissions: ReadonlyArray<string>;
  readonly lastActiveAt: string | null;
}

export interface CoverageGap {
  readonly kind: CoverageGapKind;
  readonly description: string;
  readonly window?: string;
}

export interface MemberMatrix {
  readonly members: ReadonlyArray<MemberMatrixRow>;
  readonly coverageGaps: ReadonlyArray<CoverageGap>;
  readonly accessManagementLink: {
    readonly url: string;
    readonly enabled: boolean;
  };
}
