import { useEffect, useState } from "react";
import { listTasks, createTask } from "../api/tasks.api";
import type { Task } from "../api/tasks.api";
import TaskDetailModal from "../pages/TaskDetailModal";
import { getTagColor } from "../utils/tagColor";
import { useQueryClient } from "@tanstack/react-query";
import "../styles/tasks.css";

type Props = {
    projectId: number | null;
    workspaceId: number;
    currentUserId: number; // ✅ EKLENDİ
};

const STATUSES: Task["status"][] = ["TODO", "IN_PROGRESS", "DONE"];

export default function TasksPanel({
    projectId,
    workspaceId,
    currentUserId, // ✅ PROP
}: Props) {
    const [tasks, setTasks] = useState<Task[]>([]);
    const [title, setTitle] = useState("");
    const [selectedTask, setSelectedTask] = useState<Task | null>(null);

    const qc = useQueryClient();

    // =========================
    // LOAD TASKS
    // =========================
    const load = async () => {
        if (projectId === null) return;
        const data = await listTasks(projectId);
        setTasks(data);
    };

    useEffect(() => {
        if (projectId === null) return;
        load();
    }, [projectId]);

    // =========================
    // CREATE TASK
    // =========================
    const create = async () => {
        if (projectId === null || !title.trim()) return;

        await createTask(projectId, title.trim(), "", []);
        setTitle("");
        await load();

        qc.invalidateQueries({ queryKey: ["activities"] });
    };

    // 🔥 MODAL KAPANINCA TEK DOĞRU YER
    const reloadAll = async () => {
        await load();
        setSelectedTask(null);
    };

    if (projectId === null) {
        return <div className="panel muted">Select a project</div>;
    }

    return (
        <>
            <div className="tasks-board">
                {STATUSES.map((status) => (
                    <div key={status} className="task-column">
                        <div className="task-column-title">{status}</div>

                        <div className="task-list">
                            {tasks
                                .filter((t) => t.status === status)
                                .map((t) => (
                                    <div
                                        key={t.id}
                                        className="task-card"
                                        onClick={() => setSelectedTask(t)}
                                    >
                                        <div className="task-title">
                                            {t.title}
                                        </div>

                                        {/* TAGS */}
                                        {t.tags.length > 0 && (
                                            <div className="task-tags">
                                                {t.tags.map((tag) => (
                                                    <span
                                                        key={tag}
                                                        className="task-tag"
                                                        style={{
                                                            backgroundColor:
                                                                getTagColor(tag),
                                                        }}
                                                    >
                                                        {tag}
                                                    </span>
                                                ))}
                                            </div>
                                        )}
                                    </div>
                                ))}
                        </div>

                        {status === "TODO" && (
                            <div className="task-create">
                                <input
                                    className="task-input"
                                    placeholder="New task"
                                    value={title}
                                    onChange={(e) =>
                                        setTitle(e.target.value)
                                    }
                                />
                                <button
                                    className="send-btn"
                                    onClick={create}
                                    aria-label="Add task"
                                >
                                    <svg
                                        width="18"
                                        height="18"
                                        viewBox="0 0 24 24"
                                        fill="none"
                                        stroke="white"
                                        strokeWidth="2.5"
                                        strokeLinecap="round"
                                        strokeLinejoin="round"
                                    >
                                        <line x1="12" y1="19" x2="12" y2="5" />
                                        <polyline points="5 12 12 5 19 12" />
                                    </svg>
                                </button>
                            </div>
                        )}
                    </div>
                ))}
            </div>

            {/* =========================
               TASK DETAIL MODAL
            ========================= */}
            {selectedTask && projectId !== null && (
                <TaskDetailModal
                    task={selectedTask}
                    workspaceId={workspaceId}
                    projectId={projectId}
                    currentUserId={currentUserId}
                    onClose={() => setSelectedTask(null)}
                    onUpdated={reloadAll}
                />
            )}
        </>
    );
}
