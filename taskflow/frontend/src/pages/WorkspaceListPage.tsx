import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import {
    listMyWorkspaces,
    createWorkspace,
} from "../api/workspaces.api";
import AppShell from "../components/layout/AppShell";
import "../styles/workspace.css";
import type {
    Workspace,
} from "../api/workspaces.api";

export default function WorkspaceListPage() {
    const [items, setItems] = useState<Workspace[]>([]);
    const [name, setName] = useState("");
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const load = async () => {
        setItems(await listMyWorkspaces());
    };

    useEffect(() => {
        const init = async () => {
            await load();
        };
        init();
    }, []);

    const create = async () => {
        if (!name.trim()) return;
        setLoading(true);
        await createWorkspace(name.trim());
        setName("");
        await load();
        setLoading(false);
    };

    return (
        <AppShell>
            <div className="workspace-page">
                <h1 className="page-title">My Workspaces</h1>

                <div className="workspace-create">
                    <input
                        className="workspace-input"
                        placeholder="Workspace name"
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                        data-testid="workspace-name-input"
                    />
                    <button
                        className="workspace-btn"
                        onClick={create}
                        disabled={loading}
                        data-testid="workspace-create-btn"
                    >
                        {loading ? "Creating..." : "Create"}
                    </button>
                </div>

                <div className="workspace-grid">
                    {items.map((w) => (
                        <div
                            key={w.id}
                            className="workspace-card"
                            onClick={() => navigate(`/workspaces/${w.id}`)}
                            data-testid={`workspace-${w.id}`}
                        >
                            <div className="workspace-name">{w.name}</div>
                            <div className="workspace-role">{w.role}</div>
                        </div>
                    ))}
                </div>
            </div>
        </AppShell>
    );
}
