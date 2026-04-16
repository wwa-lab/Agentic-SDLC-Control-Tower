import type { Lineage } from '@/shared/types/lineage';
import type { TemplateKind } from './enums';

export interface TemplateEntry {
  readonly id: string;
  readonly name: string;
  readonly version: string;
  readonly kind: TemplateKind;
  readonly lineage: Lineage;
  readonly detailLink?: string;
}

export interface ExceptionOverride {
  readonly templateId: string;
  readonly templateName: string;
  readonly overrideScope: 'PROJECT';
  readonly overrideScopeId: string;
  readonly overrideScopeName: string;
  readonly overriddenAt: string;
  readonly overriddenBy: string;
}

export interface TeamDefaultTemplates {
  readonly groups: Record<TemplateKind, ReadonlyArray<TemplateEntry>>;
  readonly exceptionOverrides: ReadonlyArray<ExceptionOverride>;
}
