import { api } from "./client";

export type AuthResponse = {
  accessToken: string;
};

export async function login(email: string, password: string) {
  const { data } = await api.post<AuthResponse>("/auth/login", {
    email,
    password,
  });
  return data.accessToken;
}

export async function register(email: string, password: string) {
  const { data } = await api.post<AuthResponse>("/auth/register", {
    email,
    password,
  });
  return data.accessToken;
}
