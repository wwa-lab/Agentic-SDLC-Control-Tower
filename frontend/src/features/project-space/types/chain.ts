import type { HealthAggregate, SdlcNodeKey } from './enums';

export interface ChainNodeHealth {
  readonly nodeKey: SdlcNodeKey;
  readonly label: string;
  readonly count: number | null;
  readonly health: HealthAggregate;
  readonly isExecutionHub: boolean;
  readonly deepLink: string;
  readonly enabled: boolean;
}

export interface SdlcChainState {
  readonly nodes: ReadonlyArray<ChainNodeHealth>;
}
