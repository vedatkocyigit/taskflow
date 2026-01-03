import { useQuery } from "@tanstack/react-query";
import { listActivities, type Activity } from "../api/activities.api";
import "../styles/panel.css";
import "../styles/activity.css";

type Props = { workspaceId: number };

export default function ActivityPanel({ workspaceId }: Props) {
    const q = useQuery<Activity[]>({
        queryKey: ["activities", workspaceId],
        queryFn: () => listActivities(workspaceId),
        refetchInterval: 2000, // ✅ 2 sn’de bir “real-time”
        refetchOnWindowFocus: true, // ✅ tab’e dönünce yenile
    });

    const items = q.data ?? [];

    return (
        <div className="panel">
            <div className="panel-title">Activity</div>

            <div className="panel-scroll">
                {q.isLoading && <div className="muted">Loading…</div>}
                {!q.isLoading && items.length === 0 && (
                    <div className="muted">No activity yet</div>
                )}

                {items.map((a, idx) => (
                    <div key={idx} className="activity-item">
                        <div className="activity-text">
                            <span className="activity-user">
                                {a.actorEmail}
                            </span>{" "}
                            {a.description}
                        </div>
                        <div className="activity-time">
                            {new Date(a.createdAt).toLocaleString()}
                        </div>
                    </div>
                ))}
            </div>
        </div>
    );
}
