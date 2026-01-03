import { useEffect, useState } from "react";
import type { Task } from "../api/tasks.api";
import { updateTaskStatus } from "../api/tasks.api";
import {
    listTags,
    createTag,
    attachTag,
    detachTag,
    type Tag,
} from "../api/tag.api";
import {
    listComments,
    addComment,
    deleteComment,
    type TaskComment,
} from "../api/comment.api";
import "../styles/task-modal.css";

type Props = {
    task: Task;
    projectId: number;
    workspaceId: number;
    currentUserId: number;
    onClose: () => void;
    onUpdated: () => void;
};

const STATUSES: Task["status"][] = ["TODO", "IN_PROGRESS", "DONE"];

export default function TaskDetailModal({
    task,
    projectId,
    workspaceId,
    currentUserId,
    onClose,
    onUpdated,
}: Props) {
    const [status, setStatus] = useState<Task["status"]>(task.status);

    // 🔹 Workspace’teki tüm tag’ler
    const [allTags, setAllTags] = useState<Tag[]>([]);
    const [tagInput, setTagInput] = useState("");

    // 🔹 Yorumlar
    const [comments, setComments] = useState<TaskComment[]>([]);
    const [commentInput, setCommentInput] = useState("");

    // =========================
    // TASK'A AİT TAG'LER (HESAPLANAN)
    // =========================
    const taskTags = allTags.filter((t) =>
        (task.tags ?? []).includes(t.name)
    );

    // =========================
    // LOAD TAGS + COMMENTS
    // =========================
    useEffect(() => {
        listTags(workspaceId).then(setAllTags);
        listComments(workspaceId, task.id).then(setComments);
    }, [workspaceId, task.id]);

    // =========================
    // STATUS
    // =========================
    const changeStatus = async (next: Task["status"]) => {
        if (next === status) return;
        setStatus(next);
        await updateTaskStatus(projectId, task.id, next);
        onUpdated();
    };

const addTag = async (name: string) => {
    const normalized = name.trim();
    if (!normalized) return;

    // 🔒 Burada kesin Tag olacak
    let existingTag = allTags.find(
        (t) => t.name.toLowerCase() === normalized.toLowerCase()
    );

    let finalTag: Tag;

    if (!existingTag) {
        // Yeni tag oluştur
        finalTag = await createTag(workspaceId, normalized);
        setAllTags((prev) => [...prev, finalTag]);
    } else {
        finalTag = existingTag;
    }

    await attachTag(projectId, task.id, finalTag.id);
    setTagInput("");
    onUpdated();
};


    const removeTag = async (tag: Tag) => {
        await detachTag(projectId, task.id, tag.id);
        onUpdated();
    };

    // =========================
    // COMMENTS
    // =========================
    const submitComment = async () => {
        if (!commentInput.trim()) return;

        const newComment = await addComment(
            workspaceId,
            task.id,
            commentInput.trim()
        );

        setComments((prev) => [...prev, newComment]);
        setCommentInput("");
        onUpdated();
    };

    const removeComment = async (commentId: number) => {
        await deleteComment(workspaceId, task.id, commentId);
        setComments((prev) => prev.filter((c) => c.id !== commentId));
        onUpdated();
    };

    return (
        <div className="modal-overlay" onClick={onClose}>
            <div
                className="task-modal"
                style={{ minHeight: "75vh" }}
                onClick={(e) => e.stopPropagation()}
            >
                {/* HEADER */}
                <div className="task-modal-header">
                    <h2>{task.title}</h2>
                    <button className="close-btn" onClick={onClose}>
                        ×
                    </button>
                </div>

                <div className="task-modal-body">
                    {/* STATUS */}
                    <div className="task-modal-section">
                        <label>Status</label>
                        <div className="status-row">
                            {STATUSES.map((s) => (
                                <span
                                    key={s}
                                    className={`status-pill ${
                                        status === s ? "active" : ""
                                    }`}
                                    onClick={() => changeStatus(s)}
                                >
                                    {s.replace("_", " ")}
                                </span>
                            ))}
                        </div>
                    </div>

                    {/* TAGS */}
                    <div className="task-modal-section">
                        <label>Tags</label>

                        {/* ✅ TASK'A AİT TAG’LER */}
                        <div className="tag-row">
                            {taskTags.map((tag) => (
                                <span
                                    key={tag.id}
                                    className="tag-pill"
                                    style={{
                                        backgroundColor:
                                            tag.color ?? "#eef2ff",
                                    }}
                                >
                                    {tag.name}
                                    <button
                                        className="tag-remove"
                                        onClick={() => removeTag(tag)}
                                    >
                                        ×
                                    </button>
                                </span>
                            ))}
                        </div>

                        {/* TAG INPUT */}
                        <input
                            className="tag-input"
                            placeholder="Add tag and press Enter"
                            value={tagInput}
                            onChange={(e) => setTagInput(e.target.value)}
                            onKeyDown={(e) => {
                                if (e.key === "Enter") {
                                    addTag(tagInput);
                                }
                            }}
                        />
                    </div>

                    {/* DESCRIPTION */}
                    {task.description && (
                        <div className="task-modal-section">
                            <label>Description</label>
                            <p className="description">{task.description}</p>
                        </div>
                    )}

                    {/* COMMENTS */}
                    <div className="task-modal-section">
                        <label>Comments</label>

                        <div className="comment-list">
                            {comments.map((c) => (
                                <div key={c.id} className="comment-item">
                                    <div className="comment-header">
                                        <span className="comment-author">
                                            {c.userEmail ??
                                                `User #${c.userId}`}
                                        </span>
                                        <span className="comment-date">
                                            {new Date(
                                                c.createdAt
                                            ).toLocaleString()}
                                        </span>

                                        {/* 🗑 SADECE SAHİBİ */}
                                        {c.userId === currentUserId && (
                                            <button
                                                className="comment-delete"
                                                onClick={() =>
                                                    removeComment(c.id)
                                                }
                                            >
                                                ×
                                            </button>
                                        )}
                                    </div>

                                    <div className="comment-content">
                                        {c.content}
                                    </div>
                                </div>
                            ))}
                        </div>

                        <input
                            className="comment-input"
                            placeholder="Write a comment and press Enter"
                            value={commentInput}
                            onChange={(e) => setCommentInput(e.target.value)}
                            onKeyDown={(e) => {
                                if (e.key === "Enter") submitComment();
                            }}
                        />
                    </div>
                </div>
            </div>
        </div>
    );
}
