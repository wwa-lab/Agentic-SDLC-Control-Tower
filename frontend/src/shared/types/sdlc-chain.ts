/**
 * SDLC chain types — shared across incident, requirement, and future modules.
 */

export type SdlcArtifactType =
  | 'requirement'
  | 'user-story'
  | 'spec'
  | 'architecture'
  | 'design'
  | 'tasks'
  | 'code'
  | 'test'
  | 'deploy';

export interface SdlcChainLink {
  readonly artifactType: SdlcArtifactType;
  readonly artifactId: string;
  readonly artifactTitle: string;
  readonly routePath: string;
}

export interface SdlcChain {
  readonly links: ReadonlyArray<SdlcChainLink>;
}
