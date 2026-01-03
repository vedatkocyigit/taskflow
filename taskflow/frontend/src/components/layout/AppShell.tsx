import type { PropsWithChildren } from "react";
import { useNavigate } from "react-router-dom";
import { useAuthStore } from "../../store/auth.store";
import "../../styles/shell.css";

export default function AppShell({ children }: PropsWithChildren) {
    const logout = useAuthStore((s) => s.logout);
    const navigate = useNavigate();

    const onLogout = () => {
        logout();
        navigate("/login");
    };

    return (
        <div className="app-shell">
            <header className="app-header">
                {/* SOL TARAF */}
                <div className="header-left">
                    <span className="logo">TaskFlow</span>
                </div>

                {/* SAĞ TARAF */}
                <div className="header-right">
                    <button className="btn logout-btn" onClick={onLogout}>
                        Logout
                    </button>
                </div>
            </header>

            <main className="app-content">{children}</main>
        </div>
    );
}
