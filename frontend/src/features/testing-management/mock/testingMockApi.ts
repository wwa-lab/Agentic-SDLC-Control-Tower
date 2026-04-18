import { ApiError } from '@/shared/api/client';
import type { CaseCardKey, CatalogCardKey, CatalogFilter, PlanCardKey, RunCardKey, TraceabilityCardKey } from '../types';
import {
  buildCaseDetailAggregate,
  buildCatalogAggregate,
  buildPlanDetailAggregate,
  buildRunDetailAggregate,
  buildTraceabilityAggregate,
} from './state';

function notFound(message: string) {
  return new ApiError(404, 'Not Found', message);
}

export async function readCatalogAggregate(filters: CatalogFilter) {
  return buildCatalogAggregate(filters);
}

export async function readCatalogSection(cardKey: CatalogCardKey, filters: CatalogFilter) {
  const aggregate = buildCatalogAggregate(filters);
  return aggregate[cardKey];
}

export async function readPlanAggregate(planId: string) {
  const aggregate = buildPlanDetailAggregate(planId);
  if (!aggregate) {
    throw notFound('Test plan not found.');
  }
  return aggregate;
}

export async function readPlanSection(cardKey: PlanCardKey, planId: string) {
  const aggregate = buildPlanDetailAggregate(planId);
  if (!aggregate) {
    throw notFound('Test plan not found.');
  }
  return aggregate[cardKey];
}

export async function readCaseAggregate(caseId: string) {
  const aggregate = buildCaseDetailAggregate(caseId);
  if (!aggregate) {
    throw notFound('Test case not found.');
  }
  return aggregate;
}

export async function readCaseSection(cardKey: CaseCardKey, caseId: string) {
  const aggregate = buildCaseDetailAggregate(caseId);
  if (!aggregate) {
    throw notFound('Test case not found.');
  }
  return aggregate[cardKey];
}

export async function readRunAggregate(runId: string) {
  const aggregate = buildRunDetailAggregate(runId);
  if (!aggregate) {
    throw notFound('Test run not found.');
  }
  return aggregate;
}

export async function readRunSection(cardKey: RunCardKey, runId: string) {
  const aggregate = buildRunDetailAggregate(runId);
  if (!aggregate) {
    throw notFound('Test run not found.');
  }
  return aggregate[cardKey];
}

export async function readTraceabilityAggregate() {
  return buildTraceabilityAggregate();
}

export async function readTraceabilitySection(cardKey: TraceabilityCardKey) {
  const aggregate = buildTraceabilityAggregate();
  return aggregate[cardKey];
}
