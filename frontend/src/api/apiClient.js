import axios from 'axios';

const apiClient = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
  timeout: 30000,
});

// Interceptor to inject Authorization Bearer token
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('fillforme_token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Interceptor to parse backend error responses gracefully
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    let message = 'An unexpected error occurred. Please try again.';

    if (error.response) {
      const data = error.response.data;
      if (typeof data === 'string') {
        message = data;
      } else if (data && data.message) {
        message = data.message;
      } else if (error.response.status === 401) {
        message = 'Your session has expired. Please log in again.';
      } else if (error.response.status === 403) {
        message = 'You do not have permission to perform this action.';
      } else if (error.response.status === 404) {
        message = 'The requested resource was not found.';
      }
    } else if (error.request) {
      message = 'Unable to connect to the server. Please check your internet connection.';
    }

    const enhancedError = new Error(message);
    enhancedError.status = error.response?.status;
    enhancedError.originalError = error;
    return Promise.reject(enhancedError);
  }
);

export default apiClient;
