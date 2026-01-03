import { useEffect, useState } from "react";
import { listProjects, createProject, } from "../api/projects.api";
import type { Project } from "../api/projects.api";
import "../styles/projects.css";

type Props = {
    workspaceId: number;
    selectedProjectId: number | null;
    onSelect: (id: number) => void;
};

export default function ProjectsPanel({
    workspaceId,
    selectedProjectId,
    onSelect,
}: Props) {
    const [items, setItems] = useState<Project[]>([]);
    const [name, setName] = useState("");

    const load = async () => {
        setItems(await listProjects(workspaceId));
    };

    useEffect(() => {
        const init = async () => {
            await load();
        };
        init();
    }, [workspaceId]);

    const create = async () => {
        if (!name.trim()) return;
        await createProject(workspaceId, name.trim());
        setName("");
        await load();
    };

    return (
        <div className="projects-panel">
            <div className="projects-title">Projects</div>

            <input
                className="project-input"
                placeholder="New project"
                value={name}
                onChange={(e) => setName(e.target.value)}
                data-testid="project-name-input"
            />

            <button
                className="project-btn"
                onClick={create}
                data-testid="project-create-btn"
            >
                Create
            </button>

            <div className="projects-list">
                {items.map((p) => (
                    <div
                        key={p.id}
                        className={`project-item ${
                            selectedProjectId === p.id ? "active" : ""
                        }`}
                        onClick={() => onSelect(p.id)}
                        data-testid={`project-${p.id}`}
                    >
                        {p.name}
                    </div>
                ))}
            </div>
        </div>
    );
}
