import apiClient from './apiClient';

export const exportApi = {
  getReviewSummary: async (sessionId) => {
    const response = await apiClient.get(`/sessions/${sessionId}/review`);
    return response.data;
  },

  submitForm: async (sessionId) => {
    const response = await apiClient.post(`/sessions/${sessionId}/submit`);
    return response.data;
  },

  exportForm: async (sessionId) => {
    const response = await apiClient.post(`/sessions/${sessionId}/export`, {}, {
      responseType: 'blob',
    });
    return response.data;
  },
};
