import { api } from "./client";

export type Activity = {
  actorEmail: string;
  action: string;
  description: string;
  createdAt: string;
};

export async function listActivities(
  workspaceId: number
): Promise<Activity[]> {
  const res = await api.get(
    `/workspaces/${workspaceId}/activities`
  );
  return res.data;
}
