import { api } from "./client";

export type UserDto = {
    id: number;
    email: string;
    roles: string[];
};

export async function getMe(): Promise<UserDto> {
    const { data } = await api.get("/users/me");
    return data;
}

export async function searchUserByEmail(
    email: string,
    workspaceId: number
): Promise<UserDto> {
    const { data } = await api.get("/users/search", {
        params: { email, workspaceId },
    });
    return data;
}
