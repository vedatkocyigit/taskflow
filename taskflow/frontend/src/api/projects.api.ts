import { api } from "./client";

export type Project = {
  id: number;
  name: string;
};

export async function listProjects(workspaceId: number): Promise<Project[]> {
  const res = await api.get(`/workspaces/${workspaceId}/projects`);
  return res.data;
}

export async function createProject(
  workspaceId: number,
  name: string
) {
  await api.post(`/workspaces/${workspaceId}/projects`, { name });
}
