import { api } from "./client";

export type Tag = {
    id: number;
    name: string;
    color?: string | null;
};

export async function listTags(workspaceId: number): Promise<Tag[]> {
    const { data } = await api.get(`/workspaces/${workspaceId}/tags`);
    return data;
}

export async function createTag(
    workspaceId: number,
    name: string
): Promise<Tag> {
    const { data } = await api.post(
        `/workspaces/${workspaceId}/tags`,
        null,
        { params: { name } }
    );
    return data;
}

export async function attachTag(
    projectId: number,
    taskId: number,
    tagId: number
): Promise<void> {
    await api.patch(
        `/projects/${projectId}/tasks/${taskId}/tags/${tagId}/attach`
    );
}

export async function detachTag(
    projectId: number,
    taskId: number,
    tagId: number
): Promise<void> {
    await api.patch(
        `/projects/${projectId}/tasks/${taskId}/tags/${tagId}/detach`
    );
}
