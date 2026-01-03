import { useParams } from "react-router-dom";
import { useEffect, useState } from "react";
import AppShell from "../components/layout/AppShell";
import ProjectsPanel from "./ProjectsPanel";
import TasksPanel from "./TasksPanel";
import ActivityPanel from "./ActivityPanel";
import MembersPanel from "./MembersPanel";
import "../styles/workspace.css";
import { getMe, type UserDto } from "../api/user.api";

export default function WorkspacePage() {
    const { workspaceId } = useParams();
    const wid = Number(workspaceId);

    //  login olmuş kullanıcı
    const [me, setMe] = useState<UserDto | null>(null);

    const [selectedProjectId, setSelectedProjectId] = useState<number | null>(null);

    // workspace id kontrolü
    if (!workspaceId || Number.isNaN(wid)) {
        return <div>Workspace not found</div>;
    }

    // =========================
    // LOAD ME (JWT ile /users/me)
    // =========================
    useEffect(() => {
        (async () => {
            const user = await getMe();
            setMe(user);
        })();
    }, []);

    // me gelmeden ekrana basma (yoksa currentUserId null olur)
    if (!me) {
        return (
            <AppShell>
                <div className="panel muted">Loading user...</div>
            </AppShell>
        );
    }

    return (
        <AppShell>
            <div className="ws-grid">
                {/* =========================
                   LEFT — PROJECTS
                ========================= */}
                <div className="ws-col">
                    <ProjectsPanel
                        workspaceId={wid}
                        selectedProjectId={selectedProjectId}
                        onSelect={setSelectedProjectId}
                    />
                </div>

                {/* =========================
                   CENTER — TASKS
                ========================= */}
                <div className="ws-col">
                    <TasksPanel
                        projectId={selectedProjectId}
                        workspaceId={wid}
                        currentUserId={me.id} // BURASI ÖNEMLİ
                    />
                </div>

                {/* =========================
                   RIGHT — MEMBERS + ACTIVITY
                ========================= */}
                <div className="ws-col ws-right">
                    <MembersPanel workspaceId={wid} />
                    <ActivityPanel workspaceId={wid} />
                </div>
            </div>
        </AppShell>
    );
}
