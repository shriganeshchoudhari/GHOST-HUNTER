import api from "./api";
import { AuthTokens, User } from "../store/slices/userSlice";

export interface LoginResponse extends AuthTokens {
  user: User;
}

export const authService = {
  login: async (email: string, password_hash: string): Promise<LoginResponse> => {
    const response = await api.post("/auth/login", { email, password_hash });
    return response.data;
  },
  register: async (email: string, username: string, password_hash: string): Promise<LoginResponse> => {
    const response = await api.post("/auth/register", { email, username, password_hash });
    return response.data;
  },
  logout: async (): Promise<void> => {
    await api.post("/auth/logout");
  },
};
