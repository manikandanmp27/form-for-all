import apiClient from './apiClient';

export const authApi = {
  login: async (email, password) => {
    const response = await apiClient.post('/auth/login', { email, password });
    return response.data;
  },

  register: async (fullName, email, password) => {
    const response = await apiClient.post('/auth/register', { fullName, email, password });
    return response.data;
  },

  getMe: async () => {
    const response = await apiClient.get('/auth/me');
    return response.data;
  },
};
