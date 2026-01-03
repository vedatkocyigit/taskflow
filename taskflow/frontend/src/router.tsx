import { Routes, Route, Navigate } from "react-router-dom";
import LoginPage from "./pages/LoginPage";
import RegisterPage from "./pages/RegisterPage";
import WorkspacePage from "./pages/WorkspacePage";
import WorkspaceListPage from "./pages/WorkspaceListPage";
import ProtectedRoute from "./components/layout/ProtectedRoute";

export default function AppRouter() {
    return (
        <Routes>
            <Route path="/login" element={<LoginPage />} />
            <Route path="/register" element={<RegisterPage />} />

            <Route
                path="/"
                element={
                    <ProtectedRoute>
                        <WorkspaceListPage />
                    </ProtectedRoute>
                }
            />

            <Route
                path="/workspaces/:workspaceId"
                element={
                    <ProtectedRoute>
                        <WorkspacePage />
                    </ProtectedRoute>
                }
            />

            <Route path="*" element={<Navigate to="/" />} />
        </Routes>
    );
}
