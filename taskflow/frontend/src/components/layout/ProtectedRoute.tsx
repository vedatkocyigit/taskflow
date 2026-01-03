import { Navigate } from "react-router-dom";
import { useAuthStore } from "../../store/auth.store";

export default function ProtectedRoute(props: { children: unknown }) {
    const token = useAuthStore((s) => s.token);

    if (!token) {
        return <Navigate to="/login" replace />;
    }

    return <>{props.children}</>;
}
