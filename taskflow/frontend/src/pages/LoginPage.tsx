import { useState } from "react";
import { useNavigate, Link } from "react-router-dom";
import { login } from "../api/auth.api";
import { useAuthStore } from "../store/auth.store";
import styles from "./Auth.module.css";

export default function LoginPage() {
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");
    const setToken = useAuthStore((s) => s.setToken);
    const navigate = useNavigate();

    const submit = async () => {
        const token = await login(email, password);
        setToken(token);
        navigate("/");
    };

    return (
        <div className={styles.wrapper} data-testid="login-page">
            <div className={styles.card}>
                <div className={styles.logo}>TaskFlow</div>
                <div className={styles.subtitle}>
                    Sign in to manage your team tasks
                </div>

                <div className={styles.field}>
                    <label className={styles.label}>Email</label>
                    <input
                        className={styles.input}
                        data-testid="login-email"
                        value={email}
                        onChange={(e) => setEmail(e.target.value)}
                        placeholder="you@company.com"
                    />
                </div>

                <div className={styles.field}>
                    <label className={styles.label}>Password</label>
                    <input
                        type="password"
                        className={styles.input}
                        data-testid="login-password"
                        value={password}
                        onChange={(e) => setPassword(e.target.value)}
                        placeholder="••••••••"
                    />
                </div>

                <button
                    className={styles.button}
                    data-testid="login-submit"
                    onClick={submit}
                >
                    Login
                </button>

                <div className={styles.footer}>
                    Don’t have an account?{" "}
                    <Link className={styles.link} to="/register">
                        Register
                    </Link>
                </div>
            </div>
        </div>
    );
}
