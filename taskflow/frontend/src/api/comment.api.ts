import { api } from "./client";

export type TaskComment = {
    id: number;
    userId: number;
    userEmail?: string; // 🔥 backend eklenince otomatik çalışacak
    content: string;
    createdAt: string;
};

export async function listComments(
    workspaceId: number,
    taskId: number
): Promise<TaskComment[]> {
    const { data } = await api.get(
        `/workspaces/${workspaceId}/tasks/${taskId}/comments`
    );
    return data;
}

export async function addComment(
    workspaceId: number,
    taskId: number,
    content: string
): Promise<TaskComment> {
    const { data } = await api.post(
        `/workspaces/${workspaceId}/tasks/${taskId}/comments`,
        { content }
    );
    return data;
}

export async function deleteComment(
    workspaceId: number,
    taskId: number,
    commentId: number
): Promise<void> {
    await api.delete(
        `/workspaces/${workspaceId}/tasks/${taskId}/comments/${commentId}`
    );
}