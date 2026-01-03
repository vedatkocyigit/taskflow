import { useEffect, useState } from "react";
import { addMember, listMembers, removeMember } from "../api/members.api";
import type { WorkspaceMember } from "../api/members.api";
import "../styles/members.css";

export default function MembersPanel({ workspaceId }: { workspaceId: number }) {
    const [items, setItems] = useState<WorkspaceMember[]>([]);
    const [email, setEmail] = useState("");
    const [loading, setLoading] = useState(false);

    const load = async () => {
        setLoading(true);
        setItems(await listMembers(workspaceId));
        setLoading(false);
    };

    useEffect(() => {
        const run = async () => {
            await load();
        };
        run();
    }, [workspaceId]);

    const add = async () => {
        if (!email.trim()) return;
        await addMember(workspaceId, email.trim());
        setEmail("");
        await load();
    };

    const remove = async (memberId: number) => {
        if (!window.confirm("Üyeyi workspace'den çıkarmak istiyor musun?"))
            return;
        await removeMember(workspaceId, memberId);
        await load();
    };

    return (
        <div className="panel members-panel">
            <div className="panel-header">
                <h3>Members</h3>
            </div>

            {/* ADD MEMBER */}
            <div className="member-add">
                <input
                    className="input input-modern"
                    placeholder="email@domain.com"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    data-testid="member-email-input"
                />

                <button
                    className="btn btn-primary btn-add"
                    onClick={add}
                    data-testid="member-add-btn"
                >
                    Add
                </button>
            </div>

            {/* LIST */}
            <div className="member-list">
                {loading && <div className="muted">Loading…</div>}

                {!loading &&
                    items.map((m) => (
                        <div key={m.memberId} className="member-row">
                            <div className="member-info">
                                <span className="member-email">{m.email}</span>
                                <span className="member-role">{m.role}</span>
                            </div>

                            {m.role !== "OWNER" && (
                                <button
                                    className="icon-btn danger"
                                    onClick={() => remove(m.memberId)}
                                    title="Remove member"
                                >
                                    ✕
                                </button>
                            )}
                        </div>
                    ))}

                {!loading && items.length === 0 && (
                    <div className="muted">No members</div>
                )}
            </div>
        </div>
    );
}
