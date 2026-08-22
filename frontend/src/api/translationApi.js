import apiClient from './apiClient';

export const translationApi = {
  translateForm: async (file, targetLanguage, sourceLanguage = 'auto', textRegions = []) => {
    const formData = new FormData();
    formData.append('file', file);
    formData.append('targetLanguage', targetLanguage);
    formData.append('sourceLanguage', sourceLanguage);
    if (textRegions && textRegions.length > 0) {
      formData.append('textRegions', JSON.stringify(textRegions));
    }

    const response = await apiClient.post('/translation/translate-form', formData, {
      headers: {
        'Content-Type': undefined,
      },
      timeout: 180000, // 3 minutes timeout for Vision AI image translation
    });
    return response.data;
  },

  getSupportedLanguages: async () => {
    const response = await apiClient.get('/translation/languages');
    return response.data;
  },
};
