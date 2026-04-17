import type { AccountableRole, OncallStatus } from './enums';

export interface RoleAssignment {
  readonly role: AccountableRole;
  readonly memberId: string | null;
  readonly displayName: string | null;
  readonly oncallStatus: OncallStatus;
  readonly backupPresent: boolean;
  readonly backupMemberId: string | null;
  readonly backupDisplayName: string | null;
}

export interface LeadershipOwnership {
  readonly assignments: ReadonlyArray<RoleAssignment>;
  readonly accessManagementLink: {
    readonly url: string;
    readonly enabled: boolean;
  };
}
