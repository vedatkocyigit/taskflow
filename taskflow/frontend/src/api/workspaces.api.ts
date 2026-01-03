import { api } from "./client";

export type Workspace = {
  id: number;
  name: string;
  role: "OWNER" | "MEMBER";
};

export async function listMyWorkspaces(): Promise<Workspace[]> {
  const res = await api.get("/workspaces");
  return res.data;
}

export async function createWorkspace(name: string): Promise<void> {
  await api.post("/workspaces", { name });
}
