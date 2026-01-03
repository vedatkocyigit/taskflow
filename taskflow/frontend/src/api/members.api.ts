import { api } from "./client";

export type WorkspaceMember = {
  memberId: number;   // ✅ KRİTİK
  userId: number;
  email: string;
  role: "OWNER" | "MEMBER";
};

export async function listMembers(
  workspaceId: number
): Promise<WorkspaceMember[]> {
  const { data } = await api.get(
    `/workspaces/${workspaceId}/members`
  );
  return data;
}

export async function addMember(
  workspaceId: number,
  email: string,
  role: string = "MEMBER"
) {
  await api.post(
    `/workspaces/${workspaceId}/members`,
    null,
    { params: { email, role } }
  );
}

export async function removeMember(
  workspaceId: number,
  memberId: number
) {
  await api.delete(
    `/workspaces/${workspaceId}/members/${memberId}`
  );
}
