import { fetchJson, postJson } from '@/shared/api/client';
import type { IncidentList, IncidentDetail, ActionApprovalResult } from '../types/incident';

export const incidentApi = {
  async getIncidentList(): Promise<IncidentList> {
    return fetchJson<IncidentList>('/incidents');
  },

  async getIncidentDetail(id: string): Promise<IncidentDetail> {
    return fetchJson<IncidentDetail>(`/incidents/${id}`);
  },

  async approveAction(incidentId: string, actionId: string): Promise<ActionApprovalResult> {
    return postJson<ActionApprovalResult>(`/incidents/${incidentId}/actions/${actionId}/approve`);
  },

  async rejectAction(incidentId: string, actionId: string, reason: string): Promise<ActionApprovalResult> {
    return postJson<ActionApprovalResult>(`/incidents/${incidentId}/actions/${actionId}/reject`, { reason });
  },
};
