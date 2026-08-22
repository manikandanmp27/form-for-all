import apiClient from './apiClient';

export const chatApi = {
  sendMessage: async (message, history = []) => {
    const response = await apiClient.post('/chat', {
      message,
      history,
    });
    return response.data;
  },
};
