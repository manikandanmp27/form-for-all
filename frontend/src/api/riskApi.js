import apiClient from './apiClient';

export const riskApi = {
  getRiskAlerts: async (sessionId) => {
    const response = await apiClient.get(`/sessions/${sessionId}/risk`);
    return response.data;
  },

  confirmRiskAlert: async (sessionId, riskId, confirmed) => {
    const response = await apiClient.post(`/sessions/${sessionId}/risk/${riskId}/confirm`, {
      confirmed,
    });
    return response.data;
  },
};
