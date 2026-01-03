import { api } from "./client";

export type Task = {
  id: number;
  title: string;
  description?: string;
  status: "TODO" | "IN_PROGRESS" | "DONE";
  projectId: number;
  tags: string[];
};

// =========================
// LIST TASKS
// =========================
export async function listTasks(
  projectId: number
): Promise<Task[]> {
  const res = await api.get(
    `/projects/${projectId}/tasks`
  );
  return res.data;
}

// =========================
// CREATE TASK
// =========================
export async function createTask(
  projectId: number,
  title: string,
  description: string = "",
  tags: string[] = []
) {
  await api.post(
    `/projects/${projectId}/tasks`,
    {
      title,
      description,
      tags,
    }
  );
}

// =========================
// UPDATE STATUS
// =========================
export async function updateTaskStatus(
  projectId: number,
  taskId: number,
  status: Task["status"]
) {
  await api.patch(
    `/projects/${projectId}/tasks/${taskId}/status`,
    { status }
  );
}
