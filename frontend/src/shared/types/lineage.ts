export type LineageOrigin =
  | 'PLATFORM'
  | 'APPLICATION'
  | 'SNOW_GROUP'
  | 'WORKSPACE'
  | 'PROJECT'
  | 'AI_SKILL';

export interface LineageHop {
  readonly origin: LineageOrigin;
  readonly value: string;
  readonly setAt?: string;
  readonly setBy?: string;
}

export interface Lineage {
  readonly origin: LineageOrigin;
  readonly overridden: boolean;
  readonly chain: ReadonlyArray<LineageHop>;
}
