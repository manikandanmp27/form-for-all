import apiClient from './apiClient';

export const conversationApi = {
  getConversationState: async (sessionId) => {
    const response = await apiClient.get(`/sessions/${sessionId}/conversation`);
    return response.data;
  },

  submitAnswer: async (sessionId, fieldId, answerValue, direction = 'NEXT') => {
    const response = await apiClient.post(`/sessions/${sessionId}/conversation`, {
      fieldId,
      answerValue,
      direction,
    });
    return response.data;
  },
};
