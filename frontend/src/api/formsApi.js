import apiClient from './apiClient';

export const formsApi = {
  uploadFile: async (file, formTitle, customFields = []) => {
    const formData = new FormData();
    formData.append('file', file);
    if (formTitle) {
      formData.append('title', formTitle);
      formData.append('formTitle', formTitle);
    }
    if (customFields && customFields.length > 0) {
      formData.append('customFields', JSON.stringify(customFields));
    }
    // Do NOT pass hardcoded 'Content-Type': 'multipart/form-data' so Axios dynamically sets boundary header
    const response = await apiClient.post('/forms', formData);
    return response.data;
  },

  submitUrl: async (formUrl, formTitle) => {
    const response = await apiClient.post('/forms', { formUrl, formTitle });
    return response.data;
  },

  getAllForms: async () => {
    const response = await apiClient.get('/forms');
    return response.data;
  },

  getFormById: async (sessionId) => {
    const response = await apiClient.get(`/forms/${sessionId}`);
    return response.data;
  },
};
