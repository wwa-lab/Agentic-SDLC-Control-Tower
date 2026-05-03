import { fetchJson, postJson } from './client';
import type { HelpLinks, SupportRequest, SupportRequestResult } from '@/shared/types/shell';

export function getHelpLinks(): Promise<HelpLinks> {
  return fetchJson<HelpLinks>('/shell/help-links');
}

export function submitSupportRequest(request: SupportRequest): Promise<SupportRequestResult> {
  return postJson<SupportRequestResult>('/support/contact', request);
}
